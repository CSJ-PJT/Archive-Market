package com.csj.archive.market.simulation;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.TraceIdFilter;
import java.time.LocalDate;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulations")
public class MarketSimulationController {

    private final MarketSimulationService simulationService;

    public MarketSimulationController(MarketSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/demand")
    ApiResponse<SimulationResult> demand(@RequestParam(defaultValue = "100") int count) {
        return ApiResponse.ok(simulationService.demand(count), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/orders")
    ApiResponse<SimulationResult> orders(@RequestParam(defaultValue = "100") int count) {
        return ApiResponse.ok(simulationService.orders(count), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/profitability")
    ApiResponse<SimulationResult> profitability(@RequestParam(defaultValue = "100") int count) {
        return ApiResponse.ok(simulationService.profitability(count), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/day/run")
    ApiResponse<SimulationResult> day(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(simulationService.runDay(date), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/workday/run")
    ApiResponse<SimulationResult> workday(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(simulationService.runWorkday(date), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
