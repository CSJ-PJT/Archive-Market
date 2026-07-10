package com.csj.archive.market.order;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.PageResponse;
import com.csj.archive.market.common.TraceIdFilter;
import com.csj.archive.market.simulation.MarketSimulationService;
import com.csj.archive.market.simulation.SimulationResult;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class MarketOrderController {

    private final MarketOrderService orderService;
    private final MarketSimulationService simulationService;

    public MarketOrderController(MarketOrderService orderService, MarketSimulationService simulationService) {
        this.orderService = orderService;
        this.simulationService = simulationService;
    }

    @PostMapping
    ApiResponse<MarketOrderEntity> create(@Valid @RequestBody(required = false) CreateOrderRequest request) {
        return ApiResponse.ok(orderService.create(request == null
                ? new CreateOrderRequest(null, null, 1, false)
                : request), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/simulate")
    ApiResponse<SimulationResult> simulate(@RequestParam(defaultValue = "100") int count) {
        return ApiResponse.ok(simulationService.orders(count), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping
    ApiResponse<PageResponse<MarketOrderEntity>> orders(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(orderService.list(pageable)), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping("/{orderId}")
    ApiResponse<MarketOrderEntity> order(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.get(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/{orderId}/confirm")
    ApiResponse<MarketOrderEntity> confirm(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.confirm(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/{orderId}/cancel")
    ApiResponse<MarketOrderEntity> cancel(@PathVariable String orderId) {
        return ApiResponse.ok(orderService.cancel(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
