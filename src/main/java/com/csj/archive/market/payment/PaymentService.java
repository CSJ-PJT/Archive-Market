package com.csj.archive.market.payment;

import com.csj.archive.market.audit.AuditAction;
import com.csj.archive.market.audit.AuditLogService;
import com.csj.archive.market.common.BusinessException;
import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.order.OrderStatus;
import com.csj.archive.market.product.PricingPolicy;
import com.csj.archive.market.revenue.CostType;
import com.csj.archive.market.revenue.MarketEconomyService;
import com.csj.archive.market.revenue.RevenueType;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final MarketPaymentRepository paymentRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketEconomyService economyService;
    private final PricingPolicy pricingPolicy;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public PaymentService(MarketPaymentRepository paymentRepository, MarketOrderRepository orderRepository,
                          MarketEconomyService economyService, PricingPolicy pricingPolicy,
                          AuditLogService auditLogService, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.economyService = economyService;
        this.pricingPolicy = pricingPolicy;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<MarketPaymentEntity> list() {
        return paymentRepository.findAll();
    }

    @Transactional
    public MarketPaymentEntity capture(String orderId) {
        MarketOrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        MarketPaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> paymentRepository.save(new MarketPaymentEntity(
                        IdGenerator.prefixed("PAY"), orderId, order.getPaymentAmount(), "SYNTHETIC_METHOD")));
        if (payment.getPaymentStatus() == PaymentStatus.CAPTURED || payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return payment;
        }
        payment.capture(Instant.now(clock));
        order.changeStatus(OrderStatus.PAYMENT_CAPTURED);
        String simulationRunId = IdGenerator.prefixed("SIM");
        economyService.recordRevenue(RevenueType.PAYMENT_CAPTURED, order.getPaymentAmount(), order,
                simulationRunId, null, "Synthetic payment captured");
        economyService.recordRevenue(RevenueType.PRODUCT_SALES_REVENUE_RECOGNIZED, order.getPaymentAmount(), order,
                simulationRunId, null, "Synthetic product sales revenue from Archive-Market");
        if (order.getCustomerType().name().equals("B2B_CUSTOMER")) {
            economyService.recordRevenue(RevenueType.B2B_CONTRACT_REVENUE_RECOGNIZED,
                    order.getPaymentAmount().multiply(java.math.BigDecimal.valueOf(0.03)), order,
                    simulationRunId, null, "Synthetic B2B contract uplift revenue");
        }
        economyService.recordCost(CostType.PAYMENT_PROCESSING_FEE_PAID, pricingPolicy.paymentFee(order.getPaymentAmount()),
                order, simulationRunId, null, "Synthetic payment processing fee");
        economyService.enqueuePaymentCaptured(order, simulationRunId);
        auditLogService.record(AuditAction.PAYMENT_CAPTURED, "MARKET_PAYMENT", payment.getPaymentId(), null,
                PaymentStatus.CAPTURED.name(), "Synthetic payment captured");
        return payment;
    }

    @Transactional
    public MarketPaymentEntity refund(String orderId) {
        MarketOrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        MarketPaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return payment;
        }
        if (payment.getPaymentStatus() != PaymentStatus.CAPTURED) {
            throw new BusinessException("PAYMENT_NOT_CAPTURED", "Only captured payments can be refunded");
        }
        payment.refund(Instant.now(clock));
        order.changeStatus(OrderStatus.REFUNDED);
        economyService.enqueueRefundRequested(order, IdGenerator.prefixed("SIM"));
        auditLogService.record(AuditAction.PAYMENT_REFUNDED, "MARKET_PAYMENT", payment.getPaymentId(),
                PaymentStatus.CAPTURED.name(), PaymentStatus.REFUNDED.name(), "Synthetic refund requested");
        return payment;
    }
}
