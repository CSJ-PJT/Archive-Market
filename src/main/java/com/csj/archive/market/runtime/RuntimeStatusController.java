package com.csj.archive.market.runtime;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runtime")
public class RuntimeStatusController {

    private final RuntimeAutoRunService runtimeAutoRunService;

    public RuntimeStatusController(RuntimeAutoRunService runtimeAutoRunService) {
        this.runtimeAutoRunService = runtimeAutoRunService;
    }

    @GetMapping("/status")
    ApiResponse<RuntimeStatusResponse> status() {
        return ApiResponse.ok(runtimeAutoRunService.status(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
