package com.csj.archive.market.profitability;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/risk-profile")
public class CustomerRiskProfileController {

    private final CustomerRiskProfileService riskProfileService;

    public CustomerRiskProfileController(CustomerRiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    @GetMapping
    ApiResponse<CustomerRiskProfileEntity> profile(@PathVariable String customerId) {
        return ApiResponse.ok(riskProfileService.get(customerId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/recalculate")
    ApiResponse<CustomerRiskProfileEntity> recalculate(@PathVariable String customerId) {
        return ApiResponse.ok(riskProfileService.recalculate(customerId), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
