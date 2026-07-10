package com.csj.archive.market.profitability;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderProfitabilityController {

    private final OrderProfitabilityService profitabilityService;

    public OrderProfitabilityController(OrderProfitabilityService profitabilityService) {
        this.profitabilityService = profitabilityService;
    }

    @PostMapping("/api/orders/{orderId}/profitability/evaluate")
    ApiResponse<OrderProfitabilityAssessmentEntity> evaluate(@PathVariable String orderId) {
        return ApiResponse.ok(profitabilityService.evaluate(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/orders/{orderId}/profitability")
    ApiResponse<OrderProfitabilityAssessmentEntity> assessment(@PathVariable String orderId) {
        return ApiResponse.ok(profitabilityService.get(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/market-profitability/summary")
    ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(profitabilityService.summary(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/market-profitability/assessments")
    ApiResponse<List<OrderProfitabilityAssessmentEntity>> assessments() {
        return ApiResponse.ok(profitabilityService.assessments(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
