package com.csj.archive.market.claim;

import com.csj.archive.market.audit.AuditAction;
import com.csj.archive.market.audit.AuditLogService;
import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.order.OrderStatus;
import com.csj.archive.market.revenue.CostType;
import com.csj.archive.market.revenue.MarketEconomyService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnClaimService {

    private final MarketReturnRepository returnRepository;
    private final MarketClaimRepository claimRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketEconomyService economyService;
    private final AuditLogService auditLogService;

    public ReturnClaimService(MarketReturnRepository returnRepository, MarketClaimRepository claimRepository,
                              MarketOrderRepository orderRepository, MarketEconomyService economyService,
                              AuditLogService auditLogService) {
        this.returnRepository = returnRepository;
        this.claimRepository = claimRepository;
        this.orderRepository = orderRepository;
        this.economyService = economyService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<MarketReturnEntity> returns() {
        return returnRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MarketClaimEntity> claims() {
        return claimRepository.findAll();
    }

    @Transactional
    public MarketReturnEntity requestReturn(String orderId) {
        MarketOrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (returnRepository.existsByOrderId(orderId)) {
            return returnRepository.findAll().stream()
                    .filter(value -> value.getOrderId().equals(orderId))
                    .findFirst()
                    .orElseThrow();
        }
        String before = order.getOrderStatus().name();
        order.changeStatus(OrderStatus.RETURN_REQUESTED);
        BigDecimal returnCost = order.getPaymentAmount().multiply(BigDecimal.valueOf(0.08));
        MarketReturnEntity created = returnRepository.save(new MarketReturnEntity(
                IdGenerator.prefixed("RET"), orderId, "Synthetic return request", order.getPaymentAmount(), "REQUESTED"));
        String simulationRunId = simulationRunIdFor(order);
        economyService.recordCost(CostType.RETURN_COST_INCURRED, returnCost, order, simulationRunId, null,
                "Synthetic return handling cost");
        economyService.enqueueRefundRequested(order, simulationRunId);
        economyService.enqueueGenericEvent("RETURN_REQUESTED", "MARKET_ORDER", order, simulationRunId,
                Map.of("orderId", orderId, "returnAmount", order.getPaymentAmount(), "reason", "Synthetic return"));
        auditLogService.record(AuditAction.RETURN_REQUESTED, "MARKET_RETURN", created.getReturnId(), before,
                OrderStatus.RETURN_REQUESTED.name(), "Synthetic return requested");
        return created;
    }

    @Transactional
    public MarketClaimEntity createClaim(String orderId) {
        MarketOrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (claimRepository.existsByOrderId(orderId)) {
            return claimRepository.findAll().stream()
                    .filter(value -> value.getOrderId().equals(orderId))
                    .findFirst()
                    .orElseThrow();
        }
        String before = order.getOrderStatus().name();
        order.changeStatus(OrderStatus.CLAIMED);
        BigDecimal claimAmount = order.getPaymentAmount().multiply(BigDecimal.valueOf(0.15));
        MarketClaimEntity created = claimRepository.save(new MarketClaimEntity(
                IdGenerator.prefixed("CLM"), orderId, "QUALITY_CLAIM", claimAmount, "CONFIRMED"));
        String simulationRunId = simulationRunIdFor(order);
        economyService.recordCost(CostType.CLAIM_COMPENSATION_COST_INCURRED, claimAmount, order, simulationRunId,
                null, "Synthetic claim compensation cost");
        economyService.enqueueClaimCompensation(order, claimAmount, simulationRunId);
        economyService.enqueueGenericEvent("QUALITY_CLAIM_CREATED", "MARKET_ORDER", order, simulationRunId,
                Map.of("orderId", orderId, "claimAmount", claimAmount, "claimType", "QUALITY_CLAIM"));
        auditLogService.record(AuditAction.CLAIM_CREATED, "MARKET_CLAIM", created.getClaimId(), before,
                OrderStatus.CLAIMED.name(), "Synthetic quality claim created");
        return created;
    }

    private String simulationRunIdFor(MarketOrderEntity order) {
        if (order.getSimulationRunId() != null && !order.getSimulationRunId().isBlank()) {
            return order.getSimulationRunId();
        }
        // Legacy orders are assigned a lineage only when a new lifecycle action occurs.
        String legacySimulationRunId = IdGenerator.prefixed("SIM");
        order.assignSimulationRunId(legacySimulationRunId);
        return legacySimulationRunId;
    }
}
