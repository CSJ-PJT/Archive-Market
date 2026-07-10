package com.csj.archive.market.profitability;

import com.csj.archive.market.product.ProductType;
import java.math.BigDecimal;

public record PriceRecommendationRequest(
        String orderId,
        ProductType productType,
        BigDecimal targetMarginRate
) {
}
