package com.csj.archive.market.revenue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "market_cost_event")
public class MarketCostEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "simulation_run_id")
    private String simulationRunId;

    @Column(name = "settlement_cycle_id")
    private String settlementCycleId;

    @Column(name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false)
    private CostType costType;

    @Column(name = "cost_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "reason", nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MarketCostEventEntity() {
    }

    public MarketCostEventEntity(String eventId, String idempotencyKey, String simulationRunId,
                                 String settlementCycleId, String orderId, CostType costType,
                                 BigDecimal costAmount, String currency, String reason) {
        this.eventId = eventId;
        this.idempotencyKey = idempotencyKey;
        this.simulationRunId = simulationRunId;
        this.settlementCycleId = settlementCycleId;
        this.orderId = orderId;
        this.costType = costType;
        this.costAmount = costAmount;
        this.currency = currency;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getSimulationRunId() {
        return simulationRunId;
    }

    public String getOrderId() {
        return orderId;
    }

    public CostType getCostType() {
        return costType;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }
}
