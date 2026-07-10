package com.csj.archive.market.revenue;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market-economy")
public class MarketEconomyController {

    private final MarketEconomyService economyService;

    public MarketEconomyController(MarketEconomyService economyService) {
        this.economyService = economyService;
    }

    @GetMapping("/summary")
    ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(economyService.summary(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/revenue-events")
    ApiResponse<List<MarketRevenueEventEntity>> revenueEvents() {
        return ApiResponse.ok(economyService.revenueEvents(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/cost-events")
    ApiResponse<List<MarketCostEventEntity>> costEvents() {
        return ApiResponse.ok(economyService.costEvents(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/profit-snapshots")
    ApiResponse<List<MarketProfitSnapshotEntity>> snapshots() {
        return ApiResponse.ok(economyService.snapshots(), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/daily-close")
    ApiResponse<MarketProfitSnapshotEntity> dailyClose(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(economyService.dailyClose(date), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
