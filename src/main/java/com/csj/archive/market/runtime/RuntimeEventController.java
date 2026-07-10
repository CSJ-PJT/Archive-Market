package com.csj.archive.market.runtime;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runtime-events")
public class RuntimeEventController {

    private final RuntimeEventService runtimeEventService;

    public RuntimeEventController(RuntimeEventService runtimeEventService) {
        this.runtimeEventService = runtimeEventService;
    }

    @GetMapping("/recent")
    ApiResponse<List<RuntimeEventResponse>> recent(@RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.ok(runtimeEventService.recent(limit), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/correlation/{correlationId}")
    ApiResponse<List<RuntimeEventResponse>> correlation(@PathVariable String correlationId) {
        return ApiResponse.ok(runtimeEventService.byCorrelationId(correlationId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/entity/{entityId}")
    ApiResponse<List<RuntimeEventResponse>> entity(@PathVariable String entityId) {
        return ApiResponse.ok(runtimeEventService.byEntityId(entityId), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
