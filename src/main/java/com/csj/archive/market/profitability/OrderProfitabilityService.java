package com.csj.archive.market.profitability;

import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.customer.SyntheticCustomer;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderItemEntity;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.outbox.MarketOutboxService;
import com.csj.archive.market.outbox.OutboxTargetService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderProfitabilityService {

    private final OrderProfitabilityAssessmentRepository assessmentRepository;
    private final ProfitabilityCostComponentAdjustmentRepository adjustmentRepository;
    private final MarketOrderRepository orderRepository;
    private final CustomerRiskProfileService riskProfileService;
    private final PricingPolicyService pricingPolicyService;
    private final MarketOutboxService outboxService;
    private final ProfitabilityProperties properties;

    public OrderProfitabilityService(OrderProfitabilityAssessmentRepository assessmentRepository,
                                     ProfitabilityCostComponentAdjustmentRepository adjustmentRepository,
                                     MarketOrderRepository orderRepository,
                                     CustomerRiskProfileService riskProfileService,
                                     PricingPolicyService pricingPolicyService,
                                     MarketOutboxService outboxService,
                                     ProfitabilityProperties properties) {
        this.assessmentRepository = assessmentRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.orderRepository = orderRepository;
        this.riskProfileService = riskProfileService;
        this.pricingPolicyService = pricingPolicyService;
        this.outboxService = outboxService;
        this.properties = properties;
    }

    @Transactional
    public OrderProfitabilityAssessmentEntity evaluate(String orderId) {
        String simulationRunId = orderRepository.findByOrderId(orderId)
                .map(MarketOrderEntity::getSimulationRunId)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> IdGenerator.prefixed("SIM"));
        return evaluate(orderId, simulationRunId);
    }

    @Transactional
    public OrderProfitabilityAssessmentEntity evaluate(String orderId, String simulationRunId) {
        if (!properties.isEnabled()) {
            return assessmentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                    .orElseThrow(() -> new NotFoundException("Profitability engine is disabled and no assessment exists: " + orderId));
        }
        return assessmentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseGet(() -> createAssessment(orderId, simulationRunId));
    }

    @Transactional(readOnly = true)
    public OrderProfitabilityAssessmentEntity get(String orderId) {
        return assessmentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new NotFoundException("Profitability assessment not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderProfitabilityAssessmentEntity> assessments() {
        return assessmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProfitabilityCostComponentAdjustmentEntity> costAdjustments(String orderId) {
        return adjustmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        long evaluated = assessmentRepository.count();
        long review = assessmentRepository.countByRecommendation(ProfitabilityRecommendation.REVIEW_REQUIRED);
        long rejected = assessmentRepository.countByRecommendation(ProfitabilityRecommendation.REJECT_RECOMMENDED);
        long accepted = assessmentRepository.countByRecommendation(ProfitabilityRecommendation.ACCEPT);
        return Map.of(
                "evaluatedOrders", evaluated,
                "profitableOrders", accepted,
                "reviewRequiredOrders", review,
                "rejectedRecommendedOrders", rejected,
                "lowMarginOrders", assessmentRepository.countByMarginRateLessThan(properties.getAcceptMarginRate()),
                "highRiskOrders", assessmentRepository.countByRiskScoreGreaterThanEqual(properties.getHighRiskScore()),
                "measuredCostAdjustments", adjustmentRepository.count(),
                "averageMarginRate", scale(assessmentRepository.averageMarginRate(), 2),
                "expectedProfit", scale(assessmentRepository.totalExpectedProfit(), 2));
    }

    @Transactional
    public OrderProfitabilityAssessmentEntity applyMeasuredCost(String orderId, CostComponentType componentType,
                                                                BigDecimal amount) {
        OrderProfitabilityAssessmentEntity assessment = assessmentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseGet(() -> createAssessment(orderId, IdGenerator.prefixed("SIM")));
        BigDecimal measuredAmount = scale(amount, 2);
        BigDecimal expectedTotalCost = totalCostWith(assessment, componentType, measuredAmount);
        BigDecimal expectedProfit = scale(assessment.getExpectedRevenue().subtract(expectedTotalCost), 2);
        BigDecimal marginRate = assessment.getExpectedRevenue().signum() == 0
                ? BigDecimal.ZERO
                : scale(expectedProfit.multiply(BigDecimal.valueOf(100))
                .divide(assessment.getExpectedRevenue(), 4, RoundingMode.HALF_UP), 4);
        Decision decision = decide(assessment, marginRate, expectedProfit);
        assessment.applyMeasuredCost(componentType, measuredAmount, expectedTotalCost, expectedProfit, marginRate,
                decision.recommendation(), decision.recommendation() == ProfitabilityRecommendation.REVIEW_REQUIRED,
                "Measured " + componentType.name() + " applied from external service: " + decision.reason());
        return assessmentRepository.save(assessment);
    }

    private OrderProfitabilityAssessmentEntity createAssessment(String orderId, String simulationRunId) {
        MarketOrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        CustomerRiskProfileEntity riskProfile = riskProfileService.recalculate(order.getCustomerId());
        BigDecimal orderAmount = scale(order.getPaymentAmount(), 2);
        BigDecimal productionCost = productionCost(order);
        BigDecimal logisticsCost = logisticsCost(order);
        BigDecimal ledgerFee = scale(orderAmount.multiply(properties.getLedgerSettlementFeeRate()).add(properties.getLedgerFixedFee()), 2);
        BigDecimal paymentFee = scale(orderAmount.multiply(properties.getPaymentProcessingFeeRate()), 2);
        BigDecimal discountCost = scale(order.getDiscountAmount(), 2);
        BigDecimal expectedReturnCost = scale(orderAmount
                .multiply(riskProfile.getReturnProbability())
                .multiply(properties.getReturnCostFactor()), 2);
        BigDecimal expectedClaimCost = scale(orderAmount
                .multiply(riskProfile.getClaimProbability())
                .multiply(properties.getClaimCostFactor()), 2);
        BigDecimal customerAcquisitionCost = scale(pricingPolicyService.customerAcquisitionCost(order.getCustomerType()), 2);
        BigDecimal marketOperationCost = scale(orderAmount.multiply(properties.getMarketOperationCostRate()), 2);
        BigDecimal expectedRevenue = expectedRevenue(order);
        BigDecimal expectedTotalCost = productionCost
                .add(logisticsCost)
                .add(ledgerFee)
                .add(paymentFee)
                .add(discountCost)
                .add(expectedReturnCost)
                .add(expectedClaimCost)
                .add(customerAcquisitionCost)
                .add(marketOperationCost);
        BigDecimal expectedProfit = scale(expectedRevenue.subtract(expectedTotalCost), 2);
        BigDecimal marginRate = expectedRevenue.signum() == 0
                ? BigDecimal.ZERO
                : scale(expectedProfit.multiply(BigDecimal.valueOf(100)).divide(expectedRevenue, 4, RoundingMode.HALF_UP), 4);
        BigDecimal riskScore = scale(BigDecimal.valueOf(order.getRiskScore()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP), 4);
        Decision decision = decide(order, marginRate, expectedProfit, riskScore,
                riskProfile.getReturnProbability(), riskProfile.getClaimProbability());
        OrderProfitabilityAssessmentEntity assessment = assessmentRepository.save(new OrderProfitabilityAssessmentEntity(
                IdGenerator.prefixed("ASM"),
                order.getOrderId(),
                order.getLastEventId(),
                simulationRunId,
                order.getCustomerId(),
                order.getCustomerType(),
                productType(order),
                orderAmount,
                expectedRevenue,
                productionCost,
                logisticsCost,
                ledgerFee,
                paymentFee,
                discountCost,
                expectedReturnCost,
                expectedClaimCost,
                customerAcquisitionCost,
                marketOperationCost,
                scale(expectedTotalCost, 2),
                expectedProfit,
                marginRate,
                riskScore,
                riskProfile.getReturnProbability(),
                riskProfile.getClaimProbability(),
                decision.recommendation(),
                decision.recommendation() == ProfitabilityRecommendation.REVIEW_REQUIRED,
                decision.reason()));
        enqueueRuntimeProjection(assessment, simulationRunId);
        enqueueReviewEvents(assessment, simulationRunId);
        return assessment;
    }

    private BigDecimal expectedRevenue(MarketOrderEntity order) {
        BigDecimal revenue = order.getPaymentAmount();
        if (hasProductType(order, "SERVICE_CONTRACT")) {
            revenue = revenue.add(order.getPaymentAmount().multiply(BigDecimal.valueOf(0.08)));
        }
        if (hasProductType(order, "PREMIUM_SUPPORT")) {
            revenue = revenue.add(BigDecimal.valueOf(25_000));
        }
        if (order.getRiskScore() >= 80) {
            revenue = revenue.add(BigDecimal.valueOf(15_000));
        }
        return scale(revenue, 2);
    }

    private BigDecimal productionCost(MarketOrderEntity order) {
        return scale(order.getItems().stream()
                .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add), 2);
    }

    private BigDecimal logisticsCost(MarketOrderEntity order) {
        BigDecimal cost = properties.getBasicLogisticsEstimate();
        if (order.isRequiresApproval() || order.getTotalOrderAmount().compareTo(properties.getLargeOrderThreshold()) >= 0) {
            cost = cost.add(properties.getUrgentLogisticsSurchargeEstimate());
        }
        if (hasProductType(order, "BATTERY_MODULE")) {
            cost = cost.add(properties.getColdChainEstimate());
        }
        return scale(cost, 2);
    }

    private Decision decide(MarketOrderEntity order, BigDecimal marginRate, BigDecimal expectedProfit,
                            BigDecimal riskScore, BigDecimal returnProbability, BigDecimal claimProbability) {
        if (expectedProfit.signum() < 0) {
            return new Decision(ProfitabilityRecommendation.REJECT_RECOMMENDED, "Expected profit is negative");
        }
        if (marginRate.compareTo(properties.getRejectMarginRate()) < 0) {
            return new Decision(ProfitabilityRecommendation.REJECT_RECOMMENDED, "Expected margin is below rejection threshold");
        }
        if (marginRate.compareTo(properties.getReviewMarginRate()) >= 0
                && marginRate.compareTo(properties.getAcceptMarginRate()) < 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Expected margin requires business review");
        }
        if (riskScore.compareTo(properties.getHighRiskScore()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Risk score exceeds review threshold");
        }
        if (order.getTotalOrderAmount().compareTo(properties.getLargeOrderThreshold()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Large order requires ArchiveOS approval");
        }
        if (order.getCustomerType() == SyntheticCustomer.HIGH_RISK_CUSTOMER) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "High risk synthetic customer");
        }
        if (returnProbability.compareTo(properties.getReturnReviewProbability()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Return probability exceeds review threshold");
        }
        if (claimProbability.compareTo(properties.getClaimReviewProbability()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Claim probability exceeds review threshold");
        }
        if (marginRate.compareTo(properties.getAcceptMarginRate()) >= 0
                && riskScore.compareTo(properties.getAcceptRiskScore()) < 0) {
            return new Decision(ProfitabilityRecommendation.ACCEPT, "Expected margin and risk are acceptable");
        }
        return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Order requires pricing review");
    }

    private Decision decide(OrderProfitabilityAssessmentEntity assessment, BigDecimal marginRate, BigDecimal expectedProfit) {
        if (expectedProfit.signum() < 0) {
            return new Decision(ProfitabilityRecommendation.REJECT_RECOMMENDED, "Expected profit is negative");
        }
        if (marginRate.compareTo(properties.getRejectMarginRate()) < 0) {
            return new Decision(ProfitabilityRecommendation.REJECT_RECOMMENDED, "Expected margin is below rejection threshold");
        }
        if (marginRate.compareTo(properties.getReviewMarginRate()) >= 0
                && marginRate.compareTo(properties.getAcceptMarginRate()) < 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Expected margin requires business review");
        }
        if (assessment.getRiskScore().compareTo(properties.getHighRiskScore()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Risk score exceeds review threshold");
        }
        if (assessment.getOrderAmount().compareTo(properties.getLargeOrderThreshold()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Large order requires ArchiveOS approval");
        }
        if (assessment.getCustomerType() == SyntheticCustomer.HIGH_RISK_CUSTOMER) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "High risk synthetic customer");
        }
        if (assessment.getReturnProbability().compareTo(properties.getReturnReviewProbability()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Return probability exceeds review threshold");
        }
        if (assessment.getClaimProbability().compareTo(properties.getClaimReviewProbability()) >= 0) {
            return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Claim probability exceeds review threshold");
        }
        if (marginRate.compareTo(properties.getAcceptMarginRate()) >= 0
                && assessment.getRiskScore().compareTo(properties.getAcceptRiskScore()) < 0) {
            return new Decision(ProfitabilityRecommendation.ACCEPT, "Expected margin and risk are acceptable");
        }
        return new Decision(ProfitabilityRecommendation.REVIEW_REQUIRED, "Order requires pricing review");
    }

    private BigDecimal totalCostWith(OrderProfitabilityAssessmentEntity assessment, CostComponentType componentType,
                                     BigDecimal amount) {
        BigDecimal production = componentType == CostComponentType.PRODUCTION_COST
                ? amount : assessment.getEstimatedProductionCost();
        BigDecimal logistics = componentType == CostComponentType.LOGISTICS_COST
                ? amount : assessment.getEstimatedLogisticsCost();
        BigDecimal ledgerFee = componentType == CostComponentType.LEDGER_SETTLEMENT_FEE
                ? amount : assessment.getEstimatedLedgerFee();
        BigDecimal paymentFee = componentType == CostComponentType.PAYMENT_PROCESSING_FEE
                ? amount : assessment.getPaymentProcessingFee();
        return scale(production
                .add(logistics)
                .add(ledgerFee)
                .add(paymentFee)
                .add(assessment.getDiscountCost())
                .add(assessment.getExpectedReturnCost())
                .add(assessment.getExpectedClaimCost())
                .add(assessment.getCustomerAcquisitionCost())
                .add(assessment.getMarketOperationCost()), 2);
    }

    private void enqueueReviewEvents(OrderProfitabilityAssessmentEntity assessment, String simulationRunId) {
        if (!properties.isSendReviewEvents() || assessment.getRecommendation() == ProfitabilityRecommendation.ACCEPT) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", assessment.getOrderId());
        payload.put("customerType", assessment.getCustomerType().name());
        payload.put("orderAmount", assessment.getOrderAmount());
        payload.put("expectedRevenue", assessment.getExpectedRevenue());
        payload.put("expectedCost", assessment.getExpectedTotalCost());
        payload.put("expectedProfit", assessment.getExpectedProfit());
        payload.put("marginRate", assessment.getMarginRate());
        payload.put("riskScore", assessment.getRiskScore());
        payload.put("recommendation", assessment.getRecommendation().name());
        payload.put("reason", assessment.getReason());
        outboxService.create(OutboxTargetService.ARCHIVE_OS, "ORDER_REQUIRES_REVIEW", "MARKET_ORDER",
                assessment.getOrderId(), simulationRunId, null, correlationId(assessment),
                "RTE-" + assessment.getAssessmentId(), payload);
        if (assessment.getMarginRate().compareTo(properties.getAcceptMarginRate()) < 0) {
            outboxService.create(OutboxTargetService.ARCHIVE_OS, "LOW_MARGIN_ORDER_DETECTED", "MARKET_ORDER",
                    assessment.getOrderId(), simulationRunId, null, correlationId(assessment),
                    "RTE-" + assessment.getAssessmentId(), payload);
        }
        if (assessment.getRiskScore().compareTo(properties.getHighRiskScore()) >= 0) {
            outboxService.create(OutboxTargetService.ARCHIVE_OS, "HIGH_RISK_ORDER_DETECTED", "MARKET_ORDER",
                    assessment.getOrderId(), simulationRunId, null, correlationId(assessment),
                    "RTE-" + assessment.getAssessmentId(), payload);
        }
    }

    private void enqueueRuntimeProjection(OrderProfitabilityAssessmentEntity assessment, String simulationRunId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", assessment.getOrderId());
        payload.put("assessmentId", assessment.getAssessmentId());
        payload.put("recommendation", assessment.getRecommendation().name());
        payload.put("marginRate", assessment.getMarginRate());
        payload.put("riskScore", assessment.getRiskScore());
        payload.put("expectedProfit", assessment.getExpectedProfit());
        payload.put("reason", assessment.getReason());
        outboxService.createArchiveOsRuntimeProjection("RTE-" + assessment.getAssessmentId(),
                "ORDER_PROFITABILITY_EVALUATED", "MARKET_ORDER", assessment.getOrderId(), simulationRunId, null,
                correlationId(assessment), assessment.getCausationEventId(), payload);
    }

    private String correlationId(OrderProfitabilityAssessmentEntity assessment) {
        return orderRepository.findByOrderId(assessment.getOrderId())
                .map(order -> order.getRootCorrelationId() == null || order.getRootCorrelationId().isBlank()
                        ? "CORR-" + order.getOrderId()
                        : order.getRootCorrelationId())
                .orElse("CORR-" + assessment.getOrderId());
    }

    private boolean hasProductType(MarketOrderEntity order, String productType) {
        return order.getItems().stream().map(MarketOrderItemEntity::getProductType).anyMatch(productType::equals);
    }

    private String productType(MarketOrderEntity order) {
        return order.getItems().stream()
                .findFirst()
                .map(MarketOrderItemEntity::getProductType)
                .orElse("UNKNOWN");
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private record Decision(ProfitabilityRecommendation recommendation, String reason) {
    }
}
