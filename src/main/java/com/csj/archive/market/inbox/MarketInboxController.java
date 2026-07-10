package com.csj.archive.market.inbox;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class MarketInboxController {

    private final MarketInboxService inboxService;

    public MarketInboxController(MarketInboxService inboxService) {
        this.inboxService = inboxService;
    }

    @PostMapping("/external")
    ApiResponse<MarketInboxEntity> receive(@Valid @RequestBody ExternalEventRequest request) {
        return ApiResponse.ok(inboxService.receive(request), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/external/bulk")
    ApiResponse<List<MarketInboxEntity>> receiveBulk(@Valid @RequestBody List<ExternalEventRequest> requests) {
        return ApiResponse.ok(inboxService.receiveBulk(requests), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/inbox")
    ApiResponse<List<MarketInboxEntity>> inbox() {
        return ApiResponse.ok(inboxService.list(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
