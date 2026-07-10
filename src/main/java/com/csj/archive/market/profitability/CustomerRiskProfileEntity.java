package com.csj.archive.market.profitability;

import com.csj.archive.market.customer.SyntheticCustomer;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "customer_risk_profile")
public class CustomerRiskProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private SyntheticCustomer customerType;

    @Column(name = "risk_level", nullable = false)
    private int riskLevel;

    @Column(name = "return_probability", nullable = false, precision = 8, scale = 4)
    private BigDecimal returnProbability;

    @Column(name = "claim_probability", nullable = false, precision = 8, scale = 4)
    private BigDecimal claimProbability;

    @Column(name = "discount_sensitivity", nullable = false, precision = 8, scale = 4)
    private BigDecimal discountSensitivity;

    @Column(name = "expected_ltv", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedLtv;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Column(name = "return_count", nullable = false)
    private long returnCount;

    @Column(name = "claim_count", nullable = false)
    private long claimCount;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerRiskProfileEntity() {
    }

    public CustomerRiskProfileEntity(String customerId, SyntheticCustomer customerType, int riskLevel,
                                     BigDecimal returnProbability, BigDecimal claimProbability,
                                     BigDecimal discountSensitivity, BigDecimal expectedLtv,
                                     long orderCount, long returnCount, long claimCount) {
        this.customerId = customerId;
        this.customerType = customerType;
        this.riskLevel = riskLevel;
        this.returnProbability = returnProbability;
        this.claimProbability = claimProbability;
        this.discountSensitivity = discountSensitivity;
        this.expectedLtv = expectedLtv;
        this.orderCount = orderCount;
        this.returnCount = returnCount;
        this.claimCount = claimCount;
    }

    public void recalculate(int riskLevel, BigDecimal returnProbability, BigDecimal claimProbability,
                            BigDecimal discountSensitivity, BigDecimal expectedLtv,
                            long orderCount, long returnCount, long claimCount) {
        this.riskLevel = riskLevel;
        this.returnProbability = returnProbability;
        this.claimProbability = claimProbability;
        this.discountSensitivity = discountSensitivity;
        this.expectedLtv = expectedLtv;
        this.orderCount = orderCount;
        this.returnCount = returnCount;
        this.claimCount = claimCount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public SyntheticCustomer getCustomerType() {
        return customerType;
    }

    public int getRiskLevel() {
        return riskLevel;
    }

    public BigDecimal getReturnProbability() {
        return returnProbability;
    }

    public BigDecimal getClaimProbability() {
        return claimProbability;
    }

    public BigDecimal getDiscountSensitivity() {
        return discountSensitivity;
    }

    public BigDecimal getExpectedLtv() {
        return expectedLtv;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public long getReturnCount() {
        return returnCount;
    }

    public long getClaimCount() {
        return claimCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
