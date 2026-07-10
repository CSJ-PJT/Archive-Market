package com.csj.archive.market.profitability;

import com.csj.archive.market.customer.SyntheticCustomer;
import com.csj.archive.market.product.ProductType;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pricing_policy")
public class PricingPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_code", nullable = false, unique = true)
    private String policyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false)
    private PricingPolicyType policyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_customer_type")
    private SyntheticCustomer targetCustomerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_product_type")
    private ProductType targetProductType;

    @Column(name = "fixed_amount", precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "rate", precision = 10, scale = 6)
    private BigDecimal rate;

    @Column(name = "threshold_amount", precision = 19, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PricingPolicyEntity() {
    }

    public PricingPolicyEntity(String policyCode, PricingPolicyType policyType, SyntheticCustomer targetCustomerType,
                               ProductType targetProductType, BigDecimal fixedAmount, BigDecimal rate,
                               BigDecimal thresholdAmount, boolean enabled) {
        this.policyCode = policyCode;
        this.policyType = policyType;
        this.targetCustomerType = targetCustomerType;
        this.targetProductType = targetProductType;
        this.fixedAmount = fixedAmount;
        this.rate = rate;
        this.thresholdAmount = thresholdAmount;
        this.enabled = enabled;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public PricingPolicyType getPolicyType() {
        return policyType;
    }

    public SyntheticCustomer getTargetCustomerType() {
        return targetCustomerType;
    }

    public ProductType getTargetProductType() {
        return targetProductType;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
