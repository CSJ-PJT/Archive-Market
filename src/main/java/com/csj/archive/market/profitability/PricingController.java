package com.csj.archive.market.profitability;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingPolicyService pricingPolicyService;

    public PricingController(PricingPolicyService pricingPolicyService) {
        this.pricingPolicyService = pricingPolicyService;
    }

    @GetMapping("/policies")
    ApiResponse<List<PricingPolicyEntity>> policies() {
        return ApiResponse.ok(pricingPolicyService.policies(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/policies/seed")
    ApiResponse<List<PricingPolicyEntity>> seed() {
        return ApiResponse.ok(pricingPolicyService.seed(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/recommend")
    ApiResponse<PriceRecommendationEntity> recommend(@RequestBody(required = false) PriceRecommendationRequest request) {
        return ApiResponse.ok(pricingPolicyService.recommend(request == null
                ? new PriceRecommendationRequest(null, null, null)
                : request), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
