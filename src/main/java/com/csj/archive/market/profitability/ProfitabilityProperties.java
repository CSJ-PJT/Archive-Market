package com.csj.archive.market.profitability;

import com.csj.archive.market.customer.SyntheticCustomer;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archive.market.profitability")
public class ProfitabilityProperties {

    private boolean enabled = true;
    private boolean blockLowMarginOrders;
    private boolean sendReviewEvents = true;
    private BigDecimal acceptMarginRate = BigDecimal.valueOf(15);
    private BigDecimal reviewMarginRate = BigDecimal.valueOf(5);
    private BigDecimal rejectMarginRate = BigDecimal.valueOf(5);
    private BigDecimal acceptRiskScore = BigDecimal.valueOf(0.6);
    private BigDecimal highRiskScore = BigDecimal.valueOf(0.75);
    private BigDecimal largeOrderThreshold = BigDecimal.valueOf(3_000_000);
    private BigDecimal returnReviewProbability = BigDecimal.valueOf(0.3);
    private BigDecimal claimReviewProbability = BigDecimal.valueOf(0.2);
    private BigDecimal paymentProcessingFeeRate = BigDecimal.valueOf(0.020);
    private BigDecimal ledgerSettlementFeeRate = BigDecimal.valueOf(0.003);
    private BigDecimal ledgerFixedFee = BigDecimal.valueOf(100);
    private BigDecimal basicLogisticsEstimate = BigDecimal.valueOf(50_000);
    private BigDecimal urgentLogisticsSurchargeEstimate = BigDecimal.valueOf(30_000);
    private BigDecimal coldChainEstimate = BigDecimal.valueOf(80_000);
    private BigDecimal returnCostFactor = BigDecimal.valueOf(0.5);
    private BigDecimal claimCostFactor = BigDecimal.valueOf(0.4);
    private BigDecimal marketOperationCostRate = BigDecimal.valueOf(0.010);
    private Map<SyntheticCustomer, BigDecimal> customerAcquisitionCost = new EnumMap<>(SyntheticCustomer.class);
    private Map<SyntheticCustomer, BigDecimal> defaultDiscountRate = new EnumMap<>(SyntheticCustomer.class);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBlockLowMarginOrders() {
        return blockLowMarginOrders;
    }

    public void setBlockLowMarginOrders(boolean blockLowMarginOrders) {
        this.blockLowMarginOrders = blockLowMarginOrders;
    }

    public boolean isSendReviewEvents() {
        return sendReviewEvents;
    }

    public void setSendReviewEvents(boolean sendReviewEvents) {
        this.sendReviewEvents = sendReviewEvents;
    }

    public BigDecimal getAcceptMarginRate() {
        return acceptMarginRate;
    }

    public void setAcceptMarginRate(BigDecimal acceptMarginRate) {
        this.acceptMarginRate = acceptMarginRate;
    }

    public BigDecimal getReviewMarginRate() {
        return reviewMarginRate;
    }

    public void setReviewMarginRate(BigDecimal reviewMarginRate) {
        this.reviewMarginRate = reviewMarginRate;
    }

    public BigDecimal getRejectMarginRate() {
        return rejectMarginRate;
    }

    public void setRejectMarginRate(BigDecimal rejectMarginRate) {
        this.rejectMarginRate = rejectMarginRate;
    }

    public BigDecimal getAcceptRiskScore() {
        return acceptRiskScore;
    }

    public void setAcceptRiskScore(BigDecimal acceptRiskScore) {
        this.acceptRiskScore = acceptRiskScore;
    }

    public BigDecimal getHighRiskScore() {
        return highRiskScore;
    }

    public void setHighRiskScore(BigDecimal highRiskScore) {
        this.highRiskScore = highRiskScore;
    }

    public BigDecimal getLargeOrderThreshold() {
        return largeOrderThreshold;
    }

    public void setLargeOrderThreshold(BigDecimal largeOrderThreshold) {
        this.largeOrderThreshold = largeOrderThreshold;
    }

    public BigDecimal getReturnReviewProbability() {
        return returnReviewProbability;
    }

    public void setReturnReviewProbability(BigDecimal returnReviewProbability) {
        this.returnReviewProbability = returnReviewProbability;
    }

    public BigDecimal getClaimReviewProbability() {
        return claimReviewProbability;
    }

    public void setClaimReviewProbability(BigDecimal claimReviewProbability) {
        this.claimReviewProbability = claimReviewProbability;
    }

    public BigDecimal getPaymentProcessingFeeRate() {
        return paymentProcessingFeeRate;
    }

    public void setPaymentProcessingFeeRate(BigDecimal paymentProcessingFeeRate) {
        this.paymentProcessingFeeRate = paymentProcessingFeeRate;
    }

    public BigDecimal getLedgerSettlementFeeRate() {
        return ledgerSettlementFeeRate;
    }

    public void setLedgerSettlementFeeRate(BigDecimal ledgerSettlementFeeRate) {
        this.ledgerSettlementFeeRate = ledgerSettlementFeeRate;
    }

    public BigDecimal getLedgerFixedFee() {
        return ledgerFixedFee;
    }

    public void setLedgerFixedFee(BigDecimal ledgerFixedFee) {
        this.ledgerFixedFee = ledgerFixedFee;
    }

    public BigDecimal getBasicLogisticsEstimate() {
        return basicLogisticsEstimate;
    }

    public void setBasicLogisticsEstimate(BigDecimal basicLogisticsEstimate) {
        this.basicLogisticsEstimate = basicLogisticsEstimate;
    }

    public BigDecimal getUrgentLogisticsSurchargeEstimate() {
        return urgentLogisticsSurchargeEstimate;
    }

    public void setUrgentLogisticsSurchargeEstimate(BigDecimal urgentLogisticsSurchargeEstimate) {
        this.urgentLogisticsSurchargeEstimate = urgentLogisticsSurchargeEstimate;
    }

    public BigDecimal getColdChainEstimate() {
        return coldChainEstimate;
    }

    public void setColdChainEstimate(BigDecimal coldChainEstimate) {
        this.coldChainEstimate = coldChainEstimate;
    }

    public BigDecimal getReturnCostFactor() {
        return returnCostFactor;
    }

    public void setReturnCostFactor(BigDecimal returnCostFactor) {
        this.returnCostFactor = returnCostFactor;
    }

    public BigDecimal getClaimCostFactor() {
        return claimCostFactor;
    }

    public void setClaimCostFactor(BigDecimal claimCostFactor) {
        this.claimCostFactor = claimCostFactor;
    }

    public BigDecimal getMarketOperationCostRate() {
        return marketOperationCostRate;
    }

    public void setMarketOperationCostRate(BigDecimal marketOperationCostRate) {
        this.marketOperationCostRate = marketOperationCostRate;
    }

    public Map<SyntheticCustomer, BigDecimal> getCustomerAcquisitionCost() {
        return customerAcquisitionCost;
    }

    public void setCustomerAcquisitionCost(Map<SyntheticCustomer, BigDecimal> customerAcquisitionCost) {
        this.customerAcquisitionCost = customerAcquisitionCost;
    }

    public Map<SyntheticCustomer, BigDecimal> getDefaultDiscountRate() {
        return defaultDiscountRate;
    }

    public void setDefaultDiscountRate(Map<SyntheticCustomer, BigDecimal> defaultDiscountRate) {
        this.defaultDiscountRate = defaultDiscountRate;
    }
}
