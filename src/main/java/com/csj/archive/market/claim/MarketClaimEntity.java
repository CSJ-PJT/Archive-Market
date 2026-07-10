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
@Table(name = "market_claim")
public class MarketClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_id", nullable = false, unique = true)
    private String claimId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "claim_type", nullable = false)
    private String claimType;

    @Column(name = "claim_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "status", nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MarketClaimEntity() {
    }

    public MarketClaimEntity(String claimId, String orderId, String claimType, BigDecimal claimAmount, String status) {
        this.claimId = claimId;
        this.orderId = orderId;
        this.claimType = claimType;
        this.claimAmount = claimAmount;
        this.status = status;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getClaimAmount() {
        return claimAmount;
    }
}
