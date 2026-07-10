package com.csj.archive.market.claim;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReturnClaimController {

    private final ReturnClaimService returnClaimService;

    public ReturnClaimController(ReturnClaimService returnClaimService) {
        this.returnClaimService = returnClaimService;
    }

    @PostMapping("/api/returns")
    ApiResponse<MarketReturnEntity> requestReturn(@RequestParam String orderId) {
        return ApiResponse.ok(returnClaimService.requestReturn(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/api/claims")
    ApiResponse<MarketClaimEntity> claim(@RequestParam String orderId) {
        return ApiResponse.ok(returnClaimService.createClaim(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/returns")
    ApiResponse<List<MarketReturnEntity>> returns() {
        return ApiResponse.ok(returnClaimService.returns(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/api/claims")
    ApiResponse<List<MarketClaimEntity>> claims() {
        return ApiResponse.ok(returnClaimService.claims(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
