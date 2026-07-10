package com.csj.archive.market.outbox;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outbox")
public class MarketOutboxController {

    private final MarketOutboxService outboxService;
    private final MarketOutboxPublisher publisher;

    public MarketOutboxController(MarketOutboxService outboxService, MarketOutboxPublisher publisher) {
        this.outboxService = outboxService;
        this.publisher = publisher;
    }

    @GetMapping("/summary")
    ApiResponse<Map<String, Long>> summary() {
        return ApiResponse.ok(outboxService.summary(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/events")
    ApiResponse<List<MarketOutboxEntity>> events() {
        return ApiResponse.ok(outboxService.list(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/publish")
    ApiResponse<List<MarketOutboxEntity>> publish() {
        return ApiResponse.ok(publisher.publishPending(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/retry-failed")
    ApiResponse<List<MarketOutboxEntity>> retryFailed() {
        return ApiResponse.ok(outboxService.markFailedForRetry(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
