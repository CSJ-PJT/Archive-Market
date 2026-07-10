package com.csj.archive.market.product;

import com.csj.archive.market.customer.SyntheticCustomer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PricingPolicy {

    public BigDecimal discountFor(SyntheticCustomer customerType, BigDecimal orderAmount) {
        BigDecimal rate = switch (customerType) {
            case DISCOUNT_SEEKER -> BigDecimal.valueOf(0.10);
            case VIP_CUSTOMER -> BigDecimal.valueOf(0.05);
            case B2B_CUSTOMER -> BigDecimal.valueOf(0.03);
            default -> BigDecimal.ZERO;
        };
        return orderAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal paymentFee(BigDecimal paymentAmount) {
        return paymentAmount.multiply(BigDecimal.valueOf(0.020)).setScale(2, RoundingMode.HALF_UP);
    }
}
