package com.csj.archive.market.revenue;

import com.csj.archive.market.claim.MarketClaimRepository;
import com.csj.archive.market.claim.MarketReturnRepository;
import com.csj.archive.market.capital.MarketCapitalService;
import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.order.OrderStatus;
import com.csj.archive.market.outbox.MarketOutboxService;
import com.csj.archive.market.outbox.OutboxTargetService;
import com.csj.archive.market.payment.MarketPaymentRepository;
import com.csj.archive.market.payment.PaymentStatus;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketEconomyService {

    private static final BigDecimal OPENING_CASH = BigDecimal.valueOf(50_000_000);
    private static final EnumSet<RevenueType> RECOGNIZED_REVENUE_TYPES = EnumSet.of(
            RevenueType.PLATFORM_FEE_REVENUE_RECOGNIZED,
            RevenueType.PAYMENT_PROCESSING_FEE_REVENUE_RECOGNIZED,
            RevenueType.OPTIONAL_SERVICE_FEE_RECOGNIZED,
            RevenueType.AD_PROMOTION_REVENUE_RECOGNIZED,
            RevenueType.B2B_CONTRACT_REVENUE_RECOGNIZED,
            RevenueType.EXPRESS_ORDER_FEE_EARNED,
            RevenueType.SERVICE_CONTRACT_REVENUE_RECOGNIZED,
            RevenueType.CLAIM_RECOVERY_REVENUE_RECOGNIZED);
    private static final EnumSet<CostType> RESERVE_COST_TYPES = EnumSet.of(
            CostType.REFUND_RESERVE_BOOKED,
            CostType.CLAIM_RESERVE_BOOKED,
            CostType.RISK_RESERVE_ALLOCATED);
    private static final EnumSet<CostType> PAYABLE_COST_TYPES = EnumSet.of(
            CostType.PRODUCTION_PURCHASE_COST_INCURRED,
            CostType.LOGISTICS_FULFILLMENT_FEE_INCURRED,
            CostType.SETTLEMENT_AGENCY_FEE_INCURRED,
            CostType.CONTROL_TOWER_FEE_INCURRED);
    private static final EnumSet<CostType> OPERATING_EXPENSE_TYPES = EnumSet.of(
            CostType.CUSTOMER_ACQUISITION_COST_INCURRED,
            CostType.PAYMENT_PROCESSING_FEE_PAID,
            CostType.SETTLEMENT_AGENCY_FEE_INCURRED,
            CostType.CONTROL_TOWER_FEE_INCURRED,
            CostType.MARKET_PAYROLL_BOOKED,
            CostType.INVENTORY_HOLDING_COST_INCURRED,
            CostType.EMERGENCY_SURCHARGE_INCURRED,
            CostType.RETURN_COST_INCURRED,
            CostType.CLAIM_COMPENSATION_COST_INCURRED,
            CostType.MARKET_OPERATION_COST_INCURRED,
            CostType.BAD_DEBT_COST_INCURRED);
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.100");
    private static final BigDecimal PAYMENT_PROCESSING_REVENUE_RATE = new BigDecimal("0.024");
    private static final BigDecimal OPTIONAL_SERVICE_REVENUE_RATE = new BigDecimal("0.008");
    private static final BigDecimal B2B_SERVICE_REVENUE_RATE = new BigDecimal("0.012");

    private final MarketRevenueEventRepository revenueRepository;
    private final MarketCostEventRepository costRepository;
    private final MarketProfitSnapshotRepository snapshotRepository;
    private final MarketDailyCloseRepository dailyCloseRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketReturnRepository returnRepository;
    private final MarketClaimRepository claimRepository;
    private final MarketPaymentRepository paymentRepository;
    private final MarketOutboxService outboxService;
    private final OrderProfitabilityService profitabilityService;
    private final MarketCapitalService capitalService;
    private final Clock clock;

    public MarketEconomyService(MarketRevenueEventRepository revenueRepository, MarketCostEventRepository costRepository,
                                MarketProfitSnapshotRepository snapshotRepository,
                                MarketDailyCloseRepository dailyCloseRepository, MarketOrderRepository orderRepository,
                                MarketReturnRepository returnRepository, MarketClaimRepository claimRepository,
                                MarketPaymentRepository paymentRepository, MarketOutboxService outboxService,
                                OrderProfitabilityService profitabilityService,
                                MarketCapitalService capitalService, Clock clock) {
        this.revenueRepository = revenueRepository;
        this.costRepository = costRepository;
        this.snapshotRepository = snapshotRepository;
        this.dailyCloseRepository = dailyCloseRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
        this.outboxService = outboxService;
        this.profitabilityService = profitabilityService;
        this.capitalService = capitalService;
        this.clock = clock;
    }

    @Transactional
    public MarketRevenueEventEntity recordRevenue(RevenueType type, BigDecimal amount, MarketOrderEntity order,
                                                  String simulationRunId, String settlementCycleId, String reason) {
        String orderPart = order == null ? "NONE" : order.getOrderId();
        String key = "MARKET:REVENUE:" + type + ":" + orderPart + ":" + amount;
        if (revenueRepository.existsByIdempotencyKey(key)) {
            return null;
        }
        return revenueRepository.save(new MarketRevenueEventEntity(
                IdGenerator.prefixed("REV"),
                key,
                simulationRunId,
                settlementCycleId,
                order == null ? null : order.getOrderId(),
                type,
                amount,
                order == null ? "KRW" : order.getCurrency(),
                reason));
    }

    @Transactional
    public void enqueueArchiveOsRuntimeRevenue(MarketRevenueEventEntity event, MarketOrderEntity order,
                                               String causationId) {
        if (event == null || order == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", order.getOrderId());
        payload.put("customerId", order.getCustomerId());
        payload.put("revenueType", event.getRevenueType().name());
        payload.put("amount", event.getRevenueAmount());
        payload.put("currency", event.getCurrency());
        payload.put("reason", event.getRevenueType().name() + " generated by Archive-Market synthetic runtime");
        outboxService.createArchiveOsRuntimeProjection(event.getEventId(), event.getRevenueType().name(),
                "MARKET_ORDER", order.getOrderId(), event.getSimulationRunId(), event.getSettlementCycleId(),
                correlationId(order), causationId, payload);
    }

    @Transactional
    public MarketCostEventEntity recordCost(CostType type, BigDecimal amount, MarketOrderEntity order,
                                            String simulationRunId, String settlementCycleId, String reason) {
        String orderPart = order == null ? "NONE" : order.getOrderId();
        String key = "MARKET:COST:" + type + ":" + orderPart + ":" + amount;
        if (costRepository.existsByIdempotencyKey(key)) {
            return null;
        }
        return costRepository.save(new MarketCostEventEntity(
                IdGenerator.prefixed("COST"),
                key,
                simulationRunId,
                settlementCycleId,
                order == null ? null : order.getOrderId(),
                type,
                amount,
                order == null ? "KRW" : order.getCurrency(),
                reason));
    }

    @Transactional
    public void enqueueOrderPlaced(MarketOrderEntity order, String simulationRunId, String causationId) {
        outboxService.create(OutboxTargetService.NEXUS, "MARKET_ORDER_PLACED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, correlationId(order), causationId, orderPayload(order));
    }

    @Transactional
    public void enqueueProductionAndShipment(MarketOrderEntity order, String simulationRunId, String causationId) {
        outboxService.create(OutboxTargetService.NEXUS, "PRODUCTION_REQUESTED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, correlationId(order), causationId, orderPayload(order));
        outboxService.create(OutboxTargetService.NEXUS, "SHIPMENT_REQUESTED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, correlationId(order), causationId, orderPayload(order));
    }

    @Transactional
    public void enqueuePaymentCaptured(MarketOrderEntity order, String simulationRunId, String causationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", order.getOrderId());
        payload.put("customerId", order.getCustomerId());
        payload.put("revenueType", RevenueType.PLATFORM_FEE_REVENUE_RECOGNIZED.name());
        payload.put("amount", order.getPaymentAmount());
        payload.put("currency", order.getCurrency());
        payload.put("gmv", order.getTotalOrderAmount());
        payload.put("recognizedRevenue", recognizedRevenueFor(order));
        payload.put("reason", "Synthetic Market fee revenue from Archive-Market");
        outboxService.create(OutboxTargetService.LEDGER, "SALES_REVENUE_CONFIRMED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, correlationId(order), causationId, payload);
        outboxService.create(OutboxTargetService.LEDGER, "PAYMENT_CAPTURED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, correlationId(order), causationId, payload);
    }

    @Transactional
    public void recordFinancialRebalancingForCapturedOrder(MarketOrderEntity order, String simulationRunId) {
        BigDecimal payment = order.getPaymentAmount();
        recordRevenue(RevenueType.PLATFORM_FEE_REVENUE_RECOGNIZED, rate(payment, PLATFORM_FEE_RATE), order,
                simulationRunId, null, "Synthetic platform fee recognized; GMV is tracked separately");
        recordRevenue(RevenueType.PAYMENT_PROCESSING_FEE_REVENUE_RECOGNIZED,
                rate(payment, PAYMENT_PROCESSING_REVENUE_RATE), order,
                simulationRunId, null, "Synthetic payment processing fee revenue");
        recordRevenue(RevenueType.OPTIONAL_SERVICE_FEE_RECOGNIZED,
                rate(payment, OPTIONAL_SERVICE_REVENUE_RATE), order,
                simulationRunId, null, "Synthetic optional service fee revenue");
        if (order.getCustomerType().name().equals("B2B_CUSTOMER")) {
            recordRevenue(RevenueType.B2B_CONTRACT_REVENUE_RECOGNIZED,
                    rate(payment, B2B_SERVICE_REVENUE_RATE), order,
                    simulationRunId, null, "Synthetic B2B account service fee revenue");
        }

        recordAndEnqueueCost(order, simulationRunId, CostType.PRODUCTION_PURCHASE_COST_INCURRED,
                rate(payment, "0.580"), OutboxTargetService.ARCHIVE_OS, "PRODUCTION_PURCHASE_COST_INCURRED",
                "Synthetic purchase cost payable to Archive-Nexus");
        recordAndEnqueueCost(order, simulationRunId, CostType.LOGISTICS_FULFILLMENT_FEE_INCURRED,
                rate(payment, "0.065").max(BigDecimal.valueOf(20_000)), OutboxTargetService.ARCHIVE_OS,
                "LOGISTICS_FULFILLMENT_FEE_INCURRED", "Synthetic fulfillment cost payable to Archive-Logistics");
        recordAndEnqueueCost(order, simulationRunId, CostType.SETTLEMENT_AGENCY_FEE_INCURRED,
                rate(payment, "0.003").add(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                OutboxTargetService.ARCHIVE_OS, "SETTLEMENT_AGENCY_FEE_INCURRED",
                "Synthetic settlement agency fee payable to Archive-Ledger");
        recordAndEnqueueCost(order, simulationRunId, CostType.CONTROL_TOWER_FEE_INCURRED,
                rate(payment, "0.008"), OutboxTargetService.ARCHIVE_OS, "CONTROL_TOWER_FEE_INCURRED",
                "Synthetic control tower orchestration fee payable to ArchiveOS");
        recordCost(CostType.REFUND_RESERVE_BOOKED, rate(payment, "0.015"), order, simulationRunId, null,
                "Synthetic refund reserve allocation");
        recordCost(CostType.CLAIM_RESERVE_BOOKED, rate(payment, "0.010"), order, simulationRunId, null,
                "Synthetic claim reserve allocation");
        recordCost(CostType.RISK_RESERVE_ALLOCATED, rate(payment, "0.012"), order, simulationRunId, null,
                "Synthetic risk reserve allocation");
        recordCost(CostType.MARKET_OPERATION_COST_INCURRED, rate(payment, "0.018"), order, simulationRunId, null,
                "Synthetic market operation cost");
    }

    @Transactional
    public void enqueueRefundRequested(MarketOrderEntity order, String simulationRunId) {
        Map<String, Object> payload = Map.of(
                "orderId", order.getOrderId(),
                "customerId", order.getCustomerId(),
                "amount", order.getPaymentAmount(),
                "currency", order.getCurrency(),
                "reason", "Synthetic refund request from Archive-Market");
        outboxService.create(OutboxTargetService.LEDGER, "REFUND_REQUESTED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, correlationId(order), causationId(order), payload);
    }

    @Transactional
    public void enqueueClaimCompensation(MarketOrderEntity order, BigDecimal amount, String simulationRunId) {
        Map<String, Object> payload = Map.of(
                "orderId", order.getOrderId(),
                "customerId", order.getCustomerId(),
                "amount", amount,
                "currency", order.getCurrency(),
                "reason", "Synthetic claim compensation from Archive-Market");
        outboxService.create(OutboxTargetService.LEDGER, "CLAIM_COMPENSATION_CONFIRMED", "MARKET_ORDER",
                order.getOrderId(), simulationRunId, null, correlationId(order), causationId(order), payload);
    }

    @Transactional
    public void enqueueGenericEvent(String eventType, String aggregateType, MarketOrderEntity order,
                                    String simulationRunId, Map<String, Object> payload) {
        OutboxTargetService target = switch (eventType) {
            case "ORDER_CANCELLED", "RETURN_REQUESTED", "QUALITY_CLAIM_CREATED" -> OutboxTargetService.NEXUS;
            default -> OutboxTargetService.ARCHIVE_OS;
        };
        outboxService.create(target, eventType, aggregateType, order.getOrderId(), simulationRunId, null,
                correlationId(order), causationId(order), payload);
    }

    @Transactional(readOnly = true)
    public List<MarketRevenueEventEntity> revenueEvents() {
        return revenueRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MarketCostEventEntity> costEvents() {
        return costRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MarketProfitSnapshotEntity> snapshots() {
        return snapshotRepository.findAll();
    }

    @Transactional
    public MarketProfitSnapshotEntity dailyClose(LocalDate date) {
        Map<String, Object> financials = financialSummary();
        BigDecimal revenue = (BigDecimal) financials.get("recognizedRevenue");
        BigDecimal cost = (BigDecimal) financials.get("totalExpense");
        BigDecimal profit = (BigDecimal) financials.get("operatingProfit");
        long orderCount = orderRepository.count();
        long returnCount = returnRepository.count();
        long claimCount = claimRepository.count();
        dailyCloseRepository.save(new MarketDailyCloseEntity(
                IdGenerator.prefixed("CLOSE"),
                date,
                revenue,
                cost,
                profit,
                orderCount,
                returnCount,
                claimCount,
                "COMPLETED",
                Instant.now(clock)));
        BigDecimal burnRate = profit.signum() < 0 ? profit.abs() : BigDecimal.ZERO;
        BigDecimal cash = (BigDecimal) financials.get("cashBalance");
        MarketProfitSnapshotEntity snapshot = snapshotRepository.save(new MarketProfitSnapshotEntity(
                IdGenerator.prefixed("SNAP"),
                null,
                "SETTLEMENT-" + date,
                date,
                revenue,
                cost,
                profit,
                cash,
                burnRate,
                bankruptcyRisk(cash, burnRate, profit)));
        outboxService.create(OutboxTargetService.ARCHIVE_OS, "MARKET_ECONOMY_SUMMARY_UPDATED", "MARKET_DAILY_CLOSE",
                date.toString(), null, "SETTLEMENT-" + date, IdGenerator.prefixed("CORR"), "DAILY_CLOSE",
                Map.of("date", date, "gmv", financials.get("gmv"), "recognizedRevenue", revenue,
                        "totalExpense", cost, "operatingProfit", profit,
                        "bankruptcyRisk", snapshot.getBankruptcyRisk()));
        return snapshot;
    }

    private String correlationId(MarketOrderEntity order) {
        return order.getRootCorrelationId() == null || order.getRootCorrelationId().isBlank()
                ? "CORR-" + order.getOrderId()
                : order.getRootCorrelationId();
    }

    private String causationId(MarketOrderEntity order) {
        return order.getLastEventId() == null || order.getLastEventId().isBlank()
                ? correlationId(order)
                : order.getLastEventId();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        Map<String, Object> financials = financialSummary();
        BigDecimal revenue = (BigDecimal) financials.get("recognizedRevenue");
        BigDecimal cost = (BigDecimal) financials.get("totalExpense");
        BigDecimal profit = (BigDecimal) financials.get("operatingProfit");
        long total = orderRepository.count();
        long returned = orderRepository.countByOrderStatus(OrderStatus.RETURN_REQUESTED)
                + orderRepository.countByOrderStatus(OrderStatus.REFUNDED);
        long claimed = orderRepository.countByOrderStatus(OrderStatus.CLAIMED);
        BigDecimal cash = (BigDecimal) financials.get("cashBalance");
        BigDecimal burnRate = profit.signum() < 0 ? profit.abs() : BigDecimal.ZERO;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "Archive-Market");
        result.put("status", "HEALTHY");
        result.put("orders", Map.of(
                "total", total,
                "confirmed", orderRepository.countByOrderStatus(OrderStatus.CONFIRMED),
                "cancelled", orderRepository.countByOrderStatus(OrderStatus.CANCELLED),
                "returned", returned,
                "claimed", claimed));
        Map<String, Object> economy = new LinkedHashMap<>(financials);
        economy.put("totalRevenue", revenue);
        economy.put("totalCost", cost);
        economy.put("profit", profit);
        economy.put("burnRate", burnRate);
        economy.put("bankruptcyRisk", bankruptcyRisk(cash, burnRate, profit));
        result.put("economy", economy);
        result.put("risk", Map.of(
                "returnRate", percentage(returned, total),
                "claimRate", percentage(claimed, total),
                "highRiskOrders", orderRepository.countByRiskScoreGreaterThanEqual(80)));
        result.put("profitability", profitabilityService.summary());
        result.putAll(capitalService.combinedSummary());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> financialSummary() {
        Instant calculatedAt = Instant.now(clock);
        Instant windowStart = calculatedAt.minus(Duration.ofHours(24));
        BigDecimal gmv = orderRepository.totalGmvBetween(windowStart, calculatedAt);
        BigDecimal capturedPayment = paymentRepository.totalAmountByPaymentStatusBetween(PaymentStatus.CAPTURED, windowStart, calculatedAt);
        BigDecimal grossSalesEvents = revenueRepository.totalRevenueByTypesBetween(
                List.of(RevenueType.PRODUCT_SALES_REVENUE_RECOGNIZED), windowStart, calculatedAt);
        BigDecimal recognizedRevenue = revenueRepository.totalRevenueByTypesBetween(
                RECOGNIZED_REVENUE_TYPES, windowStart, calculatedAt);
        BigDecimal totalRecordedCost = costRepository.totalCostBetween(windowStart, calculatedAt);
        BigDecimal totalExpense = costRepository.totalCostByTypesBetween(
                OPERATING_EXPENSE_TYPES, windowStart, calculatedAt);
        BigDecimal reserveBalance = costRepository.totalCostByTypesBetween(RESERVE_COST_TYPES, windowStart, calculatedAt);
        BigDecimal outstandingPayables = costRepository.totalCostByTypesBetween(PAYABLE_COST_TYPES, windowStart, calculatedAt);
        BigDecimal passThroughCost = costRepository.totalCostByTypesBetween(
                List.of(CostType.PRODUCTION_PURCHASE_COST_INCURRED,
                        CostType.LOGISTICS_FULFILLMENT_FEE_INCURRED), windowStart, calculatedAt);
        BigDecimal discountFunding = costByTypeBetween(CostType.DISCOUNT_COST_INCURRED, windowStart, calculatedAt);
        BigDecimal operatingProfit = recognizedRevenue.subtract(totalExpense);
        BigDecimal operatingMargin = percentage(operatingProfit, recognizedRevenue);
        BigDecimal pendingSettlement = capturedPayment.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cashBalance = OPENING_CASH.add(recognizedRevenue)
                .subtract(totalExpense)
                .setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> workforce = capitalService.workforceSummary();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gmv", gmv);
        result.put("grossSalesEvents", grossSalesEvents);
        result.put("recognizedRevenue", recognizedRevenue);
        result.put("totalExpense", totalExpense);
        result.put("totalRecordedCost", totalRecordedCost);
        result.put("passThroughCost", passThroughCost);
        result.put("discountFunding", discountFunding);
        result.put("operatingProfit", operatingProfit);
        result.put("operatingMargin", operatingMargin);
        result.put("cashBalance", cashBalance);
        result.put("reserveBalance", reserveBalance);
        result.put("outstandingPayables", outstandingPayables);
        result.put("pendingSettlementAmount", pendingSettlement);
        result.put("workforceCost", costByTypeBetween(CostType.MARKET_PAYROLL_BOOKED, windowStart, calculatedAt));
        result.put("productionPurchaseCost", costByTypeBetween(CostType.PRODUCTION_PURCHASE_COST_INCURRED, windowStart, calculatedAt));
        result.put("logisticsFulfillmentCost", costByTypeBetween(CostType.LOGISTICS_FULFILLMENT_FEE_INCURRED, windowStart, calculatedAt));
        result.put("settlementAgencyFee", costByTypeBetween(CostType.SETTLEMENT_AGENCY_FEE_INCURRED, windowStart, calculatedAt));
        result.put("controlTowerFee", costByTypeBetween(CostType.CONTROL_TOWER_FEE_INCURRED, windowStart, calculatedAt));
        result.put("backlogCount", workforce.get("backlog"));
        result.put("capacityUtilization", workforce.get("capacityUtilization"));
        result.put("negativeProfitStreak", negativeProfitStreak(operatingProfit));
        result.putAll(calculationMetadata(windowStart, calculatedAt));
        result.put("cashDeltaReason", cashDeltaReason(recognizedRevenue, totalExpense, cashBalance));
        result.put("topRevenueDrivers", topRevenueDrivers(windowStart, calculatedAt));
        result.put("topExpenseDrivers", topExpenseDrivers(windowStart, calculatedAt));
        result.put("topRecordedCostDrivers", topRecordedCostDrivers(windowStart, calculatedAt));
        result.put("currency", "SYNTHETIC_KRW");
        return result;
    }

    private Map<String, Object> orderPayload(MarketOrderEntity order) {
        MarketOrderEntity loaded = orderRepository.findByOrderId(order.getOrderId()).orElse(order);
        String productType = loaded.getItems().isEmpty() ? "UNKNOWN" : loaded.getItems().getFirst().getProductType();
        int quantity = loaded.getItems().isEmpty() ? 0 : loaded.getItems().getFirst().getQuantity();
        return Map.of(
                "orderId", loaded.getOrderId(),
                "customerType", loaded.getCustomerType().name(),
                "productType", productType,
                "quantity", quantity,
                "orderAmount", loaded.getPaymentAmount(),
                "priority", loaded.isRequiresApproval() ? "HIGH" : "NORMAL",
                "requiresShipment", true);
    }

    private BigDecimal percentage(long value, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> calculationMetadata(Instant windowStart, Instant calculatedAt) {
        List<Instant> latest = List.of(
                revenueRepository.findLatestCreatedAtBetween(windowStart, calculatedAt),
                costRepository.findLatestCreatedAtBetween(windowStart, calculatedAt)).stream()
                .flatMap(java.util.Optional::stream)
                .toList();
        Map<String, Object> metadata = new LinkedHashMap<>();
        Instant sourceLatestEventAt = latest.stream().max(Instant::compareTo).orElse(null);
        metadata.put("calculationScope", "ROLLING_24H_RECOGNIZED_EVENTS");
        metadata.put("periodStart", windowStart);
        metadata.put("periodEnd", calculatedAt);
        metadata.put("calculatedAt", calculatedAt);
        metadata.put("sourceLatestEventAt", sourceLatestEventAt);
        metadata.put("dataAvailable", sourceLatestEventAt != null);
        return metadata;
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal costByTypeBetween(CostType costType, Instant from, Instant to) {
        return costRepository.totalCostByTypesBetween(List.of(costType), from, to);
    }

    private long negativeProfitStreak(BigDecimal currentOperatingProfit) {
        if (currentOperatingProfit.signum() >= 0) {
            return 0;
        }
        long recordedStreak = 0;
        for (MarketDailyCloseEntity close : dailyCloseRepository.findAllByOrderByCloseDateDescCreatedAtDesc()) {
            if (close.getTotalProfit().signum() >= 0) {
                break;
            }
            recordedStreak++;
        }
        return recordedStreak == 0 ? 1 : recordedStreak;
    }

    private void recordAndEnqueueCost(MarketOrderEntity order, String simulationRunId, CostType costType,
                                      BigDecimal amount, OutboxTargetService target, String eventType, String reason) {
        recordCost(costType, amount, order, simulationRunId, null, reason);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", order.getOrderId());
        payload.put("customerId", order.getCustomerId());
        payload.put("costType", costType.name());
        payload.put("amount", amount);
        payload.put("currency", order.getCurrency());
        payload.put("reason", reason);
        payload.put("synthetic", true);
        outboxService.create(target, eventType, "MARKET_ORDER", order.getOrderId(), simulationRunId, null,
                correlationId(order), causationId(order), payload);
    }

    private BigDecimal recognizedRevenueFor(MarketOrderEntity order) {
        BigDecimal payment = order.getPaymentAmount();
        BigDecimal revenue = rate(payment, PLATFORM_FEE_RATE
                .add(PAYMENT_PROCESSING_REVENUE_RATE)
                .add(OPTIONAL_SERVICE_REVENUE_RATE));
        if (order.getCustomerType().name().equals("B2B_CUSTOMER")) {
            revenue = revenue.add(rate(payment, B2B_SERVICE_REVENUE_RATE));
        }
        return revenue.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal amount, String rate) {
        return amount.multiply(new BigDecimal(rate)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private String cashDeltaReason(BigDecimal recognizedRevenue, BigDecimal totalExpense, BigDecimal cashBalance) {
        return "Opening synthetic cash plus fee-based recognized revenue, minus Market-owned realized operating "
                + "expense. Ecosystem pass-through payables, discounts already reflected in payment, reserve "
                + "allocations, and pending settlement are disclosed separately and are not double-counted in P&L. "
                + "recognizedRevenue=" + recognizedRevenue + ", totalExpense=" + totalExpense
                + ", cashBalance=" + cashBalance;
    }

    private List<Map<String, Object>> topRevenueDrivers(Instant from, Instant to) {
        Map<RevenueType, BigDecimal> totals = new EnumMap<>(RevenueType.class);
        revenueRepository.findAll().stream()
                .filter(event -> event.getCreatedAt() != null
                        && !event.getCreatedAt().isBefore(from) && !event.getCreatedAt().isAfter(to))
                .filter(event -> RECOGNIZED_REVENUE_TYPES.contains(event.getRevenueType()))
                .forEach(event -> totals.merge(event.getRevenueType(), event.getRevenueAmount(), BigDecimal::add));
        return totals.entrySet().stream()
                .sorted(Map.Entry.<RevenueType, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> Map.<String, Object>of("type", entry.getKey().name(), "amount", entry.getValue()))
                .toList();
    }

    private List<Map<String, Object>> topExpenseDrivers(Instant from, Instant to) {
        return costDrivers(from, to, true, 5);
    }

    private List<Map<String, Object>> topRecordedCostDrivers(Instant from, Instant to) {
        return costDrivers(from, to, false, 8);
    }

    private List<Map<String, Object>> costDrivers(Instant from, Instant to, boolean operatingOnly, int limit) {
        Map<CostType, BigDecimal> totals = new EnumMap<>(CostType.class);
        costRepository.findAll().stream()
                .filter(event -> event.getCreatedAt() != null
                        && !event.getCreatedAt().isBefore(from) && !event.getCreatedAt().isAfter(to))
                .filter(event -> !operatingOnly || OPERATING_EXPENSE_TYPES.contains(event.getCostType()))
                .forEach(event -> totals.merge(event.getCostType(), event.getCostAmount(), BigDecimal::add));
        return totals.entrySet().stream()
                .sorted(Map.Entry.<CostType, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> Map.<String, Object>of("type", entry.getKey().name(), "amount", entry.getValue()))
                .toList();
    }

    private String bankruptcyRisk(BigDecimal cash, BigDecimal burnRate, BigDecimal profit) {
        if (cash.signum() < 0 || burnRate.compareTo(BigDecimal.valueOf(10_000_000)) > 0) {
            return "HIGH";
        }
        if (profit.signum() < 0 || cash.compareTo(BigDecimal.valueOf(15_000_000)) < 0) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
