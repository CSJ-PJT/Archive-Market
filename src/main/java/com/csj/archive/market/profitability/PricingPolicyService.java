package com.csj.archive.market.profitability;

import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.customer.SyntheticCustomer;
import com.csj.archive.market.product.ProductEntity;
import com.csj.archive.market.product.ProductRepository;
import com.csj.archive.market.product.ProductType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingPolicyService {

    private final PricingPolicyRepository pricingPolicyRepository;
    private final PriceRecommendationRepository recommendationRepository;
    private final ProductRepository productRepository;
    private final ProfitabilityProperties properties;

    public PricingPolicyService(PricingPolicyRepository pricingPolicyRepository,
                                PriceRecommendationRepository recommendationRepository,
                                ProductRepository productRepository,
                                ProfitabilityProperties properties) {
        this.pricingPolicyRepository = pricingPolicyRepository;
        this.recommendationRepository = recommendationRepository;
        this.productRepository = productRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<PricingPolicyEntity> policies() {
        return pricingPolicyRepository.findByEnabledTrueOrderByPolicyCodeAsc();
    }

    @Transactional
    public List<PricingPolicyEntity> seed() {
        create("PAYMENT_PROCESSING_FEE_RATE", PricingPolicyType.FEE, null, null, null,
                properties.getPaymentProcessingFeeRate(), null);
        create("LEDGER_SETTLEMENT_FEE_RATE", PricingPolicyType.FEE, null, null, null,
                properties.getLedgerSettlementFeeRate(), null);
        create("LEDGER_FIXED_FEE", PricingPolicyType.FEE, null, null, properties.getLedgerFixedFee(), null, null);
        create("BASIC_LOGISTICS_ESTIMATE", PricingPolicyType.LOGISTICS, null, null,
                properties.getBasicLogisticsEstimate(), null, null);
        create("URGENT_LOGISTICS_SURCHARGE", PricingPolicyType.LOGISTICS, null, null,
                properties.getUrgentLogisticsSurchargeEstimate(), null, null);
        create("COLD_CHAIN_ESTIMATE", PricingPolicyType.LOGISTICS, null, ProductType.BATTERY_MODULE,
                properties.getColdChainEstimate(), null, null);
        for (SyntheticCustomer type : SyntheticCustomer.values()) {
            create("CAC_" + type.name(), PricingPolicyType.FEE, type, null,
                    customerAcquisitionCost(type), null, null);
            create("DISCOUNT_" + type.name(), PricingPolicyType.DISCOUNT, type, null, null,
                    properties.getDefaultDiscountRate().getOrDefault(type, BigDecimal.ZERO), null);
        }
        create("LARGE_ORDER_REVIEW_THRESHOLD", PricingPolicyType.THRESHOLD, null, null, null, null,
                properties.getLargeOrderThreshold());
        return policies();
    }

    @Transactional
    public PriceRecommendationEntity recommend(PriceRecommendationRequest request) {
        ProductType productType = request.productType() == null ? ProductType.BATTERY_MODULE : request.productType();
        ProductEntity product = productRepository.findByEnabledTrue().stream()
                .filter(item -> item.getProductType() == productType)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found for type: " + productType));
        BigDecimal targetMargin = request.targetMarginRate() == null
                ? properties.getAcceptMarginRate()
                : request.targetMarginRate();
        BigDecimal divisor = BigDecimal.ONE.subtract(targetMargin.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal minAcceptable = product.getBaseCost().divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal recommended = minAcceptable.max(product.getBasePrice());
        return recommendationRepository.save(new PriceRecommendationEntity(
                IdGenerator.prefixed("PRICE"),
                request.orderId(),
                productType,
                product.getBasePrice(),
                recommended,
                minAcceptable,
                targetMargin,
                "Synthetic target margin pricing recommendation"));
    }

    private void create(String code, PricingPolicyType type, SyntheticCustomer customerType, ProductType productType,
                        BigDecimal fixedAmount, BigDecimal rate, BigDecimal threshold) {
        if (!pricingPolicyRepository.existsByPolicyCode(code)) {
            pricingPolicyRepository.save(new PricingPolicyEntity(
                    code, type, customerType, productType, fixedAmount, rate, threshold, true));
        }
    }

    BigDecimal customerAcquisitionCost(SyntheticCustomer customerType) {
        return properties.getCustomerAcquisitionCost().getOrDefault(customerType, BigDecimal.valueOf(5_000));
    }
}
