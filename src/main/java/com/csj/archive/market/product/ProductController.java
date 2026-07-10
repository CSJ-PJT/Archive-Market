package com.csj.archive.market.product;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    ApiResponse<List<ProductEntity>> products() {
        return ApiResponse.ok(productService.list(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/seed")
    ApiResponse<List<ProductEntity>> seed() {
        return ApiResponse.ok(productService.seed(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
