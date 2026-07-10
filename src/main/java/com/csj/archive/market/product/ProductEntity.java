package com.csj.archive.market.product;

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
@Table(name = "market_product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "base_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseCost;

    @Column(name = "margin_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal marginRate;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProductEntity() {
    }

    public ProductEntity(String productId, ProductType productType, String productName,
                         BigDecimal basePrice, BigDecimal baseCost, BigDecimal marginRate, boolean enabled) {
        this.productId = productId;
        this.productType = productType;
        this.productName = productName;
        this.basePrice = basePrice;
        this.baseCost = baseCost;
        this.marginRate = marginRate;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public ProductType getProductType() {
        return productType;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public BigDecimal getMarginRate() {
        return marginRate;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
