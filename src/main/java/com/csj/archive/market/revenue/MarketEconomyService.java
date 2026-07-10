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
import com.csj.archive.market.profitability.OrderProfitabilityService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketEconomyService {

    private final MarketRevenueEventRepository revenueRepository;
    private final MarketCostEventRepository costRepository;
    private final MarketProfitSnapshotRepository snapshotRepository;
    private final MarketDailyCloseRepository dailyCloseRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketReturnRepository returnRepository;
    private final MarketClaimRepository claimRepository;
    private final MarketOutboxService outboxService;
    private final OrderProfitabilityService profitabilityService;
    private final MarketCapitalService capitalService;
    private final Clock clock;

    public MarketEconomyService(MarketRevenueEventRepository revenueRepository, MarketCostEventRepository costRepository,
                                MarketProfitSnapshotRepository snapshotRepository,
                                MarketDailyCloseRepository dailyCloseRepository, MarketOrderRepository orderRepository,
                                MarketReturnRepository returnRepository, MarketClaimRepository claimRepository,
                                MarketOutboxService outboxService, OrderProfitabilityService profitabilityService,
                                MarketCapitalService capitalService, Clock clock) {
        this.revenueRepository = revenueRepository;
        this.costRepository = costRepository;
        this.snapshotRepository = snapshotRepository;
        this.dailyCloseRepository = dailyCloseRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.claimRepository = claimRepository;
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
    public void enqueueOrderPlaced(MarketOrderEntity order, String simulationRunId) {
        outboxService.create(OutboxTargetService.NEXUS, "MARKET_ORDER_PLACED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), orderPayload(order));
    }

    @Transactional
    public void enqueueProductionAndShipment(MarketOrderEntity order, String simulationRunId) {
        outboxService.create(OutboxTargetService.NEXUS, "PRODUCTION_REQUESTED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), orderPayload(order));
        outboxService.create(OutboxTargetService.NEXUS, "SHIPMENT_REQUESTED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), orderPayload(order));
    }

    @Transactional
    public void enqueuePaymentCaptured(MarketOrderEntity order, String simulationRunId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderId", order.getOrderId());
        payload.put("customerId", order.getCustomerId());
        payload.put("revenueType", RevenueType.PRODUCT_SALES_REVENUE_RECOGNIZED.name());
        payload.put("amount", order.getPaymentAmount());
        payload.put("currency", order.getCurrency());
        payload.put("reason", "Synthetic product sales revenue from Archive-Market");
        outboxService.create(OutboxTargetService.LEDGER, "SALES_REVENUE_CONFIRMED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), payload);
        outboxService.create(OutboxTargetService.LEDGER, "PAYMENT_CAPTURED", "MARKET_ORDER", order.getOrderId(),
                simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), payload);
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
                simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), payload);
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
                order.getOrderId(), simulationRunId, null, IdGenerator.prefixed("CORR"), order.getOrderId(), payload);
    }

    @Transactional
    public void enqueueGenericEvent(String eventType, String aggregateType, MarketOrderEntity order,
                                    String simulationRunId, Map<String, Object> payload) {
        OutboxTargetService target = switch (eventType) {
            case "ORDER_CANCELLED", "RETURN_REQUESTED", "QUALITY_CLAIM_CREATED" -> OutboxTargetService.NEXUS;
            default -> OutboxTargetService.ARCHIVE_OS;
        };
        outboxService.create(target, eventType, aggregateType, order.getOrderId(), simulationRunId, null,
                IdGenerator.prefixed("CORR"), order.getOrderId(), payload);
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
        BigDecimal revenue = revenueRepository.totalRevenue();
        BigDecimal cost = costRepository.totalCost();
        BigDecimal profit = revenue.subtract(cost);
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
        BigDecimal cash = BigDecimal.valueOf(50_000_000).add(profit);
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
                Map.of("date", date, "revenue", revenue, "cost", cost, "profit", profit,
                        "bankruptcyRisk", snapshot.getBankruptcyRisk()));
        return snapshot;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        BigDecimal revenue = revenueRepository.totalRevenue();
        BigDecimal cost = costRepository.totalCost();
        BigDecimal profit = revenue.subtract(cost);
        long total = orderRepository.count();
        long returned = orderRepository.countByOrderStatus(OrderStatus.RETURN_REQUESTED)
                + orderRepository.countByOrderStatus(OrderStatus.REFUNDED);
        long claimed = orderRepository.countByOrderStatus(OrderStatus.CLAIMED);
        BigDecimal cash = BigDecimal.valueOf(50_000_000).add(profit);
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
        result.put("economy", Map.of(
                "totalRevenue", revenue,
                "totalCost", cost,
                "profit", profit,
                "cashBalance", cash,
                "burnRate", burnRate,
                "bankruptcyRisk", bankruptcyRisk(cash, burnRate, profit)));
        result.put("risk", Map.of(
                "returnRate", percentage(returned, total),
                "claimRate", percentage(claimed, total),
                "highRiskOrders", orderRepository.countByRiskScoreGreaterThanEqual(80)));
        result.put("profitability", profitabilityService.summary());
        result.putAll(capitalService.combinedSummary());
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
