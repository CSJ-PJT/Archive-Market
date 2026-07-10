package com.csj.archive.market.profitability;

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

@Entity
@Table(name = "price_recommendation")
public class PriceRecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recommendation_id", nullable = false, unique = true)
    private String recommendationId;

    @Column(name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "recommended_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal recommendedPrice;

    @Column(name = "min_acceptable_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal minAcceptablePrice;

    @Column(name = "target_margin_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal targetMarginRate;

    @Column(name = "reason", nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PriceRecommendationEntity() {
    }

    public PriceRecommendationEntity(String recommendationId, String orderId, ProductType productType,
                                     BigDecimal basePrice, BigDecimal recommendedPrice,
                                     BigDecimal minAcceptablePrice, BigDecimal targetMarginRate, String reason) {
        this.recommendationId = recommendationId;
        this.orderId = orderId;
        this.productType = productType;
        this.basePrice = basePrice;
        this.recommendedPrice = recommendedPrice;
        this.minAcceptablePrice = minAcceptablePrice;
        this.targetMarginRate = targetMarginRate;
        this.reason = reason;
    }

    public String getRecommendationId() {
        return recommendationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public ProductType getProductType() {
        return productType;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getRecommendedPrice() {
        return recommendedPrice;
    }

    public BigDecimal getMinAcceptablePrice() {
        return minAcceptablePrice;
    }

    public BigDecimal getTargetMarginRate() {
        return targetMarginRate;
    }

    public String getReason() {
        return reason;
    }
}
