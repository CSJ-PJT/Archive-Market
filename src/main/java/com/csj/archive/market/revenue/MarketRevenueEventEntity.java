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
@Table(name = "market_revenue_event")
public class MarketRevenueEventEntity {

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
    @Column(name = "revenue_type", nullable = false)
    private RevenueType revenueType;

    @Column(name = "revenue_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal revenueAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "reason", nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MarketRevenueEventEntity() {
    }

    public MarketRevenueEventEntity(String eventId, String idempotencyKey, String simulationRunId,
                                    String settlementCycleId, String orderId, RevenueType revenueType,
                                    BigDecimal revenueAmount, String currency, String reason) {
        this.eventId = eventId;
        this.idempotencyKey = idempotencyKey;
        this.simulationRunId = simulationRunId;
        this.settlementCycleId = settlementCycleId;
        this.orderId = orderId;
        this.revenueType = revenueType;
        this.revenueAmount = revenueAmount;
        this.currency = currency;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public RevenueType getRevenueType() {
        return revenueType;
    }

    public BigDecimal getRevenueAmount() {
        return revenueAmount;
    }
}
