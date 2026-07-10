package com.csj.archive.market.capital;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketCapitalController {

    private final MarketCapitalService capitalService;

    public MarketCapitalController(MarketCapitalService capitalService) {
        this.capitalService = capitalService;
    }

    @GetMapping("/api/market-cashflow/summary")
    ApiResponse<Map<String, Object>> cashflow() {
        return ApiResponse.ok(capitalService.cashflowSummary(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/market-workforce/summary")
    ApiResponse<Map<String, Object>> workforce() {
        return ApiResponse.ok(capitalService.workforceSummary(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/market-productivity/summary")
    ApiResponse<Map<String, Object>> productivity() {
        return ApiResponse.ok(capitalService.productivitySummary(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/api/market-workforce/allocate")
    ApiResponse<Map<String, Object>> allocate(@RequestBody(required = false) WorkforceAllocationRequest request) {
        return ApiResponse.ok(capitalService.allocate(request), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
