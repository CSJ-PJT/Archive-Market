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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "order_profitability_assessment")
public class OrderProfitabilityAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_id", nullable = false, unique = true)
    private String assessmentId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private SyntheticCustomer customerType;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(name = "order_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "expected_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedRevenue;

    @Column(name = "estimated_production_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal estimatedProductionCost;

    @Column(name = "estimated_logistics_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal estimatedLogisticsCost;

    @Column(name = "estimated_ledger_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal estimatedLedgerFee;

    @Column(name = "payment_processing_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentProcessingFee;

    @Column(name = "discount_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountCost;

    @Column(name = "expected_return_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedReturnCost;

    @Column(name = "expected_claim_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedClaimCost;

    @Column(name = "customer_acquisition_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal customerAcquisitionCost;

    @Column(name = "market_operation_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal marketOperationCost;

    @Column(name = "expected_total_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedTotalCost;

    @Column(name = "expected_profit", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedProfit;

    @Column(name = "margin_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal marginRate;

    @Column(name = "risk_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal riskScore;

    @Column(name = "return_probability", nullable = false, precision = 8, scale = 4)
    private BigDecimal returnProbability;

    @Column(name = "claim_probability", nullable = false, precision = 8, scale = 4)
    private BigDecimal claimProbability;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false)
    private ProfitabilityRecommendation recommendation;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Column(name = "reason", nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderProfitabilityAssessmentEntity() {
    }

    public OrderProfitabilityAssessmentEntity(String assessmentId, String orderId, String customerId,
                                              SyntheticCustomer customerType, String productType,
                                              BigDecimal orderAmount, BigDecimal expectedRevenue,
                                              BigDecimal estimatedProductionCost, BigDecimal estimatedLogisticsCost,
                                              BigDecimal estimatedLedgerFee, BigDecimal paymentProcessingFee,
                                              BigDecimal discountCost, BigDecimal expectedReturnCost,
                                              BigDecimal expectedClaimCost, BigDecimal customerAcquisitionCost,
                                              BigDecimal marketOperationCost, BigDecimal expectedTotalCost,
                                              BigDecimal expectedProfit, BigDecimal marginRate, BigDecimal riskScore,
                                              BigDecimal returnProbability, BigDecimal claimProbability,
                                              ProfitabilityRecommendation recommendation, boolean approvalRequired,
                                              String reason) {
        this.assessmentId = assessmentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerType = customerType;
        this.productType = productType;
        this.orderAmount = orderAmount;
        this.expectedRevenue = expectedRevenue;
        this.estimatedProductionCost = estimatedProductionCost;
        this.estimatedLogisticsCost = estimatedLogisticsCost;
        this.estimatedLedgerFee = estimatedLedgerFee;
        this.paymentProcessingFee = paymentProcessingFee;
        this.discountCost = discountCost;
        this.expectedReturnCost = expectedReturnCost;
        this.expectedClaimCost = expectedClaimCost;
        this.customerAcquisitionCost = customerAcquisitionCost;
        this.marketOperationCost = marketOperationCost;
        this.expectedTotalCost = expectedTotalCost;
        this.expectedProfit = expectedProfit;
        this.marginRate = marginRate;
        this.riskScore = riskScore;
        this.returnProbability = returnProbability;
        this.claimProbability = claimProbability;
        this.recommendation = recommendation;
        this.approvalRequired = approvalRequired;
        this.reason = reason;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public SyntheticCustomer getCustomerType() {
        return customerType;
    }

    public String getProductType() {
        return productType;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public BigDecimal getExpectedRevenue() {
        return expectedRevenue;
    }

    public BigDecimal getEstimatedProductionCost() {
        return estimatedProductionCost;
    }

    public BigDecimal getEstimatedLogisticsCost() {
        return estimatedLogisticsCost;
    }

    public BigDecimal getEstimatedLedgerFee() {
        return estimatedLedgerFee;
    }

    public BigDecimal getPaymentProcessingFee() {
        return paymentProcessingFee;
    }

    public BigDecimal getDiscountCost() {
        return discountCost;
    }

    public BigDecimal getExpectedReturnCost() {
        return expectedReturnCost;
    }

    public BigDecimal getExpectedClaimCost() {
        return expectedClaimCost;
    }

    public BigDecimal getCustomerAcquisitionCost() {
        return customerAcquisitionCost;
    }

    public BigDecimal getMarketOperationCost() {
        return marketOperationCost;
    }

    public BigDecimal getExpectedTotalCost() {
        return expectedTotalCost;
    }

    public BigDecimal getExpectedProfit() {
        return expectedProfit;
    }

    public BigDecimal getMarginRate() {
        return marginRate;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public BigDecimal getReturnProbability() {
        return returnProbability;
    }

    public BigDecimal getClaimProbability() {
        return claimProbability;
    }

    public ProfitabilityRecommendation getRecommendation() {
        return recommendation;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public String getReason() {
        return reason;
    }
}
