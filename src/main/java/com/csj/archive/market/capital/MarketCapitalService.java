package com.csj.archive.market.capital;

import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.order.OrderStatus;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.revenue.MarketCostEventRepository;
import com.csj.archive.market.revenue.MarketRevenueEventRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketCapitalService {

    public static final String DEFAULT_WORKDAY_ID = "DEFAULT";
    private static final BigDecimal SYNTHETIC_OPENING_CASH = BigDecimal.valueOf(50_000_000);

    private final MarketWorkforceAllocationRepository workforceRepository;
    private final MarketWorkdaySnapshotRepository snapshotRepository;
    private final MarketRevenueEventRepository revenueRepository;
    private final MarketCostEventRepository costRepository;
    private final MarketOrderRepository orderRepository;
    private final OrderProfitabilityService profitabilityService;

    public MarketCapitalService(MarketWorkforceAllocationRepository workforceRepository,
                                MarketWorkdaySnapshotRepository snapshotRepository,
                                MarketRevenueEventRepository revenueRepository,
                                MarketCostEventRepository costRepository,
                                MarketOrderRepository orderRepository,
                                OrderProfitabilityService profitabilityService) {
        this.workforceRepository = workforceRepository;
        this.snapshotRepository = snapshotRepository;
        this.revenueRepository = revenueRepository;
        this.costRepository = costRepository;
        this.orderRepository = orderRepository;
        this.profitabilityService = profitabilityService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> cashflowSummary() {
        BigDecimal revenue = revenueRepository.totalRevenue();
        BigDecimal cost = costRepository.totalCost();
        BigDecimal payroll = payrollCost();
        BigDecimal expectedReceivable = revenue.multiply(BigDecimal.valueOf(0.35)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pendingSettlement = revenue.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal productionRequestCost = revenue.multiply(BigDecimal.valueOf(0.22)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal logisticsRequestCost = BigDecimal.valueOf(orderRepository.count()).multiply(BigDecimal.valueOf(50_000));
        BigDecimal ledgerFee = revenue.multiply(BigDecimal.valueOf(0.003)).add(BigDecimal.valueOf(orderRepository.count() * 100L))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal netProfit = revenue.subtract(cost).subtract(payroll).subtract(ledgerFee);
        BigDecimal availableCash = SYNTHETIC_OPENING_CASH.add(revenue).subtract(cost).subtract(payroll)
                .subtract(pendingSettlement).subtract(productionRequestCost).subtract(logisticsRequestCost)
                .subtract(ledgerFee);
        BigDecimal workingCapital = availableCash.add(expectedReceivable).subtract(pendingSettlement);
        return ordered(Map.of(
                "availableCash", availableCash,
                "expectedReceivable", expectedReceivable,
                "pendingSettlementAmount", pendingSettlement,
                "payrollCost", payroll,
                "productionRequestCost", productionRequestCost,
                "logisticsRequestCost", logisticsRequestCost,
                "ledgerFee", ledgerFee,
                "netProfit", netProfit,
                "workingCapital", workingCapital,
                "currency", "SYNTHETIC_KRW"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> workforceSummary() {
        List<MarketWorkforceAllocationEntity> allocations = activeAllocations(DEFAULT_WORKDAY_ID);
        long orderCount = orderRepository.count();
        long capacity = processingCapacity(allocations);
        long backlog = Math.max(0, orderCount - capacity);
        long usedCapacity = Math.min(orderCount, capacity);
        BigDecimal payroll = payrollCost(allocations);
        return ordered(Map.of(
                "roles", allocations,
                "totalHeadcount", allocations.stream().mapToInt(MarketWorkforceAllocationEntity::getHeadcount).sum(),
                "orderCount", orderCount,
                "processingCapacity", capacity,
                "effectiveCapacity", capacity,
                "usedCapacity", usedCapacity,
                "backlog", backlog,
                "payrollCost", payroll,
                "capacityUtilization", percentage(usedCapacity, Math.max(1, capacity))));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> capacitySummary() {
        Map<String, Object> workforce = workforceSummary();
        return ordered(Map.of(
                "serviceName", "Archive-Market",
                "domain", "market",
                "totalHeadcount", workforce.get("totalHeadcount"),
                "effectiveCapacity", workforce.get("effectiveCapacity"),
                "usedCapacity", workforce.get("usedCapacity"),
                "backlog", workforce.get("backlog"),
                "capacityUtilization", workforce.get("capacityUtilization")));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> productivitySummary() {
        List<MarketWorkforceAllocationEntity> allocations = activeAllocations(DEFAULT_WORKDAY_ID);
        long orderCount = orderRepository.count();
        long capacity = processingCapacity(allocations);
        long backlog = Math.max(0, orderCount - capacity);
        BigDecimal productivity = productivityScore(allocations, orderCount, capacity, backlog);
        BigDecimal backlogPressure = percentage(backlog, Math.max(1, orderCount));
        BigDecimal cancellationRate = backlogPressure.multiply(BigDecimal.valueOf(0.30)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal claimRate = backlogPressure.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal delayRisk = backlogPressure.multiply(BigDecimal.valueOf(0.45)).min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        return ordered(Map.of(
                "productivityScore", productivity,
                "revenueConversion", revenueConversion(orderCount, capacity),
                "backlog", backlog,
                "backlogPressure", backlogPressure,
                "cancellationRate", cancellationRate,
                "claimRate", claimRate,
                "delayRisk", delayRisk,
                "aiAgentRecommendation", recommendation(backlog, productivity, delayRisk)));
    }

    @Transactional
    public Map<String, Object> allocate(WorkforceAllocationRequest request) {
        String workdayId = normalizeWorkdayId(request == null ? null : request.workdayId());
        if (request != null && request.allocations() != null) {
            for (Map.Entry<WorkforceRole, WorkforceAllocationRequest.RoleAllocation> entry : request.allocations().entrySet()) {
                WorkforceAllocationRequest.RoleAllocation value = entry.getValue();
                MarketWorkforceAllocationEntity allocation = workforceRepository.findByWorkdayIdAndWorkforceRole(workdayId, entry.getKey())
                        .orElseGet(() -> defaultAllocation(workdayId, entry.getKey()));
                allocation.allocate(
                        value.headcount() == null ? allocation.getHeadcount() : value.headcount(),
                        value.capacityPerDay() == null ? allocation.getCapacityPerDay() : value.capacityPerDay(),
                        value.wagePerDay() == null ? allocation.getWagePerDay() : value.wagePerDay(),
                        value.productivityScore() == null ? allocation.getProductivityScore() : value.productivityScore());
                workforceRepository.save(allocation);
            }
        }
        return workforceSummary();
    }

    @Transactional
    public MarketWorkdaySnapshotEntity runWorkday(LocalDate date) {
        Map<String, Object> cashflow = cashflowSummary();
        Map<String, Object> workforce = workforceSummary();
        Map<String, Object> productivity = productivitySummary();
        MarketWorkdaySnapshotEntity snapshot = new MarketWorkdaySnapshotEntity(
                IdGenerator.prefixed("WORKDAY"),
                date,
                ((Number) workforce.get("orderCount")).longValue(),
                ((Number) workforce.get("processingCapacity")).longValue(),
                ((Number) workforce.get("backlog")).longValue(),
                (BigDecimal) cashflow.get("availableCash"),
                (BigDecimal) cashflow.get("workingCapital"),
                (BigDecimal) cashflow.get("payrollCost"),
                (BigDecimal) cashflow.get("netProfit"),
                (BigDecimal) productivity.get("productivityScore"),
                productivity.get("aiAgentRecommendation").toString());
        return snapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> combinedSummary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cashflow", cashflowSummary());
        result.put("workforce", workforceSummary());
        result.put("productivity", productivitySummary());
        result.put("lastWorkday", snapshotRepository.findTopByOrderByWorkDateDescCreatedAtDesc().orElse(null));
        return result;
    }

    @Transactional
    public synchronized void seedDefaults() {
        seedDefaults(DEFAULT_WORKDAY_ID);
    }

    @Transactional
    public synchronized void seedDefaults(String workdayId) {
        String normalizedWorkdayId = normalizeWorkdayId(workdayId);
        for (WorkforceRole role : WorkforceRole.values()) {
            if (workforceRepository.findByWorkdayIdAndWorkforceRole(normalizedWorkdayId, role).isEmpty()) {
                try {
                    workforceRepository.save(defaultAllocation(normalizedWorkdayId, role));
                } catch (DataIntegrityViolationException ignored) {
                    // Another request seeded the same synthetic role concurrently.
                }
            }
        }
    }

    private MarketWorkforceAllocationEntity defaultAllocation(WorkforceRole role) {
        return defaultAllocation(DEFAULT_WORKDAY_ID, role);
    }

    private MarketWorkforceAllocationEntity defaultAllocation(String workdayId, WorkforceRole role) {
        return switch (role) {
            case ORDER_OPERATOR -> allocation(workdayId, role, 4, 45, "120000", "0.86");
            case PRICING_ANALYST -> allocation(workdayId, role, 2, 28, "180000", "0.82");
            case CUSTOMER_SUPPORT -> allocation(workdayId, role, 3, 35, "110000", "0.78");
            case CLAIM_HANDLER -> allocation(workdayId, role, 2, 22, "130000", "0.76");
            case MARKET_MANAGER -> allocation(workdayId, role, 1, 60, "220000", "0.84");
        };
    }

    private MarketWorkforceAllocationEntity allocation(String workdayId, WorkforceRole role, int headcount, int capacityPerDay,
                                                       String wage, String productivity) {
        return new MarketWorkforceAllocationEntity(workdayId, role, headcount, capacityPerDay,
                new BigDecimal(wage), new BigDecimal(productivity), true);
    }

    private List<MarketWorkforceAllocationEntity> activeAllocations(String workdayId) {
        return workforceRepository.findByWorkdayIdAndEnabledTrueOrderByWorkforceRoleAsc(normalizeWorkdayId(workdayId));
    }

    private long processingCapacity(List<MarketWorkforceAllocationEntity> allocations) {
        return allocations.stream()
                .mapToLong(item -> Math.round(item.getHeadcount() * item.getCapacityPerDay()
                        * item.getProductivityScore().doubleValue()))
                .sum();
    }

    private BigDecimal payrollCost() {
        return payrollCost(activeAllocations(DEFAULT_WORKDAY_ID));
    }

    private BigDecimal payrollCost(List<MarketWorkforceAllocationEntity> allocations) {
        return allocations.stream()
                .map(item -> item.getWagePerDay().multiply(BigDecimal.valueOf(item.getHeadcount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal productivityScore(List<MarketWorkforceAllocationEntity> allocations, long orderCount,
                                         long capacity, long backlog) {
        if (allocations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = allocations.stream()
                .map(MarketWorkforceAllocationEntity::getProductivityScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allocations.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal utilizationPenalty = percentage(Math.max(0, orderCount - capacity), Math.max(1, capacity))
                .multiply(BigDecimal.valueOf(0.35));
        BigDecimal backlogPenalty = BigDecimal.valueOf(backlog).multiply(BigDecimal.valueOf(0.08));
        return base.subtract(utilizationPenalty).subtract(backlogPenalty).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal revenueConversion(long orderCount, long capacity) {
        if (orderCount == 0) {
            return BigDecimal.ZERO;
        }
        return percentage(Math.min(orderCount, capacity), orderCount);
    }

    private BigDecimal percentage(long value, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private String recommendation(long backlog, BigDecimal productivity, BigDecimal delayRisk) {
        if (backlog > 50) {
            return "인력 증원과 고위험 주문 보류를 권장합니다.";
        }
        if (delayRisk.compareTo(BigDecimal.valueOf(20)) > 0) {
            return "클레임 처리 우선순위 조정과 주문 처리 capacity 증설을 권장합니다.";
        }
        if (productivity.compareTo(BigDecimal.valueOf(70)) < 0) {
            return "할인 축소와 pricing analyst 배정을 재조정하십시오.";
        }
        return "현재 workforce capacity로 정상 처리 가능합니다.";
    }

    private Map<String, Object> ordered(Map<String, Object> source) {
        return new LinkedHashMap<>(source);
    }

    private String normalizeWorkdayId(String workdayId) {
        return workdayId == null || workdayId.isBlank() ? DEFAULT_WORKDAY_ID : workdayId;
    }
}
