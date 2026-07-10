package com.csj.archive.market.operations;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations")
public class OperationsSummaryController {

    private final OperationsSummaryService operationsSummaryService;

    public OperationsSummaryController(OperationsSummaryService operationsSummaryService) {
        this.operationsSummaryService = operationsSummaryService;
    }

    @GetMapping("/summary")
    ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(operationsSummaryService.summary(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
