package com.csj.archive.market.claim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "market_return")
public class MarketReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_id", nullable = false, unique = true)
    private String returnId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "return_reason", nullable = false)
    private String returnReason;

    @Column(name = "return_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal returnAmount;

    @Column(name = "status", nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MarketReturnEntity() {
    }

    public MarketReturnEntity(String returnId, String orderId, String returnReason, BigDecimal returnAmount, String status) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.returnReason = returnReason;
        this.returnAmount = returnAmount;
        this.status = status;
    }

    public String getReturnId() {
        return returnId;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }
}
