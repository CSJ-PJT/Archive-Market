package com.csj.archive.market.payment;

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
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/capture")
    ApiResponse<MarketPaymentEntity> capture(@RequestParam String orderId) {
        return ApiResponse.ok(paymentService.capture(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/refund")
    ApiResponse<MarketPaymentEntity> refund(@RequestParam String orderId) {
        return ApiResponse.ok(paymentService.refund(orderId), MDC.get(TraceIdFilter.TRACE_ID));
    }

    @GetMapping
    ApiResponse<List<MarketPaymentEntity>> payments() {
        return ApiResponse.ok(paymentService.list(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
