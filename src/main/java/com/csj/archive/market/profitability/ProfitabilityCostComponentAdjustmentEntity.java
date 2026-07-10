package com.csj.archive.market.profitability;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "profitability_cost_component_adjustment")
public class ProfitabilityCostComponentAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "adjustment_id", nullable = false, unique = true)
    private String adjustmentId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @Column(name = "source_event_id", nullable = false, unique = true)
    private String sourceEventId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private CostComponentType componentType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProfitabilityCostComponentAdjustmentEntity() {
    }

    public ProfitabilityCostComponentAdjustmentEntity(String adjustmentId, String orderId, String sourceService,
                                                      String sourceEventId, String idempotencyKey,
                                                      CostComponentType componentType, BigDecimal amount,
                                                      String currency, String payload, Instant appliedAt) {
        this.adjustmentId = adjustmentId;
        this.orderId = orderId;
        this.sourceService = sourceService;
        this.sourceEventId = sourceEventId;
        this.idempotencyKey = idempotencyKey;
        this.componentType = componentType;
        this.amount = amount;
        this.currency = currency;
        this.payload = payload;
        this.appliedAt = appliedAt;
    }

    public String getAdjustmentId() {
        return adjustmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public CostComponentType getComponentType() {
        return componentType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
