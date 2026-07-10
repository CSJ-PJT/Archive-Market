package com.csj.archive.market.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateOrderRequest(
        String customerId,
        String productId,
        @Min(1) @Max(1000) Integer quantity,
        Boolean express
) {
}
