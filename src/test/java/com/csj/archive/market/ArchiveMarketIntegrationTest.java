package com.csj.archive.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.csj.archive.market.claim.ReturnClaimService;
import com.csj.archive.market.common.BusinessException;
import com.csj.archive.market.customer.CustomerEntity;
import com.csj.archive.market.customer.CustomerService;
import com.csj.archive.market.inbox.ExternalEventRequest;
import com.csj.archive.market.inbox.MarketInboxRepository;
import com.csj.archive.market.inbox.MarketInboxService;
import com.csj.archive.market.inbox.MarketInboxStatus;
import com.csj.archive.market.order.CreateOrderRequest;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.order.MarketOrderService;
import com.csj.archive.market.outbox.MarketOutboxPublisher;
import com.csj.archive.market.outbox.MarketOutboxRepository;
import com.csj.archive.market.outbox.OutboxStatus;
import com.csj.archive.market.payment.MarketPaymentRepository;
import com.csj.archive.market.payment.PaymentService;
import com.csj.archive.market.payment.PaymentStatus;
import com.csj.archive.market.product.ProductEntity;
import com.csj.archive.market.product.ProductType;
import com.csj.archive.market.product.ProductService;
import com.csj.archive.market.profitability.OrderProfitabilityAssessmentEntity;
import com.csj.archive.market.profitability.OrderProfitabilityAssessmentRepository;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.profitability.ProfitabilityRecommendation;
import com.csj.archive.market.revenue.CostType;
import com.csj.archive.market.revenue.MarketCostEventRepository;
import com.csj.archive.market.revenue.MarketEconomyService;
import com.csj.archive.market.revenue.MarketProfitSnapshotRepository;
import com.csj.archive.market.revenue.MarketRevenueEventRepository;
import com.csj.archive.market.revenue.RevenueType;
import com.csj.archive.market.simulation.MarketSimulationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class ArchiveMarketIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("archive_market_test")
            .withUsername("archive_market")
            .withPassword("archive_market");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("market.integration.enabled", () -> "false");
    }

    @Autowired ProductService productService;
    @Autowired CustomerService customerService;
    @Autowired MarketOrderService orderService;
    @Autowired PaymentService paymentService;
    @Autowired ReturnClaimService returnClaimService;
    @Autowired MarketSimulationService simulationService;
    @Autowired MarketEconomyService economyService;
    @Autowired MarketInboxService inboxService;
    @Autowired OrderProfitabilityService profitabilityService;
    @Autowired MarketOutboxPublisher outboxPublisher;
    @Autowired MarketOrderRepository orderRepository;
    @Autowired MarketPaymentRepository paymentRepository;
    @Autowired MarketRevenueEventRepository revenueRepository;
    @Autowired MarketCostEventRepository costRepository;
    @Autowired MarketOutboxRepository outboxRepository;
    @Autowired MarketInboxRepository inboxRepository;
    @Autowired MarketProfitSnapshotRepository snapshotRepository;
    @Autowired OrderProfitabilityAssessmentRepository assessmentRepository;
    @Autowired MockMvc mockMvc;

    @Test
    void marketCommerceFlowUsesSyntheticEventsOutboxInboxAndSummaries() throws Exception {
        var seeded = productService.seed();
        assertThat(seeded).hasSize(5);

        CustomerEntity discountCustomer = customerService.createSynthetic(4);
        ProductEntity product = seeded.getFirst();

        MarketOrderEntity discountedOrder = orderService.create(new CreateOrderRequest(
                discountCustomer.getCustomerId(), product.getProductId(), 2, true));
        orderService.confirm(discountedOrder.getOrderId());
        paymentService.capture(discountedOrder.getOrderId());

        assertThat(orderRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(paymentRepository.countByPaymentStatus(PaymentStatus.CAPTURED)).isGreaterThanOrEqualTo(1);
        assertThat(revenueRepository.findAll())
                .extracting("revenueType")
                .contains(RevenueType.PRODUCT_SALES_REVENUE_RECOGNIZED, RevenueType.EXPRESS_ORDER_FEE_EARNED);
        assertThat(costRepository.findAll())
                .extracting("costType")
                .contains(CostType.DISCOUNT_COST_INCURRED, CostType.PAYMENT_PROCESSING_FEE_PAID);
        assertThat(outboxRepository.count()).isGreaterThan(0);

        var simulated = simulationService.orders(3);
        assertThat(simulated.ordersCreated()).isEqualTo(3);
        assertThat(simulated.paymentsCaptured()).isEqualTo(3);

        MarketOrderEntity returnOrder = orderService.create(new CreateOrderRequest(null, product.getProductId(), 1, false));
        orderService.confirm(returnOrder.getOrderId());
        paymentService.capture(returnOrder.getOrderId());
        returnClaimService.requestReturn(returnOrder.getOrderId());
        assertThat(outboxRepository.findAll())
                .extracting("eventType")
                .contains("REFUND_REQUESTED");

        MarketOrderEntity claimOrder = orderService.create(new CreateOrderRequest(null, product.getProductId(), 1, false));
        orderService.confirm(claimOrder.getOrderId());
        paymentService.capture(claimOrder.getOrderId());
        returnClaimService.createClaim(claimOrder.getOrderId());
        assertThat(costRepository.findAll())
                .extracting("costType")
                .contains(CostType.CLAIM_COMPENSATION_COST_INCURRED);

        ExternalEventRequest external = externalEvent("EXT-1", "EXT:1", 1, 5);
        inboxService.receive(external);
        assertThatThrownBy(() -> inboxService.receive(external))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already received");
        assertThat(inboxRepository.count()).isEqualTo(1);

        var rejected = inboxService.receive(externalEvent("EXT-2", "EXT:2", 6, 5));
        assertThat(rejected.getStatus()).isEqualTo(MarketInboxStatus.REJECTED);

        var snapshot = economyService.dailyClose(LocalDate.of(2026, 7, 10));
        assertThat(snapshotRepository.findAll())
                .extracting("snapshotId")
                .contains(snapshot.getSnapshotId());
        assertThat(snapshot.getProfitAmount()).isNotNull();

        outboxPublisher.publishPending();
        assertThat(outboxRepository.countByStatus(OutboxStatus.DRY_RUN)).isGreaterThan(0);

        mockMvc.perform(get("/api/market-economy/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.service").value("Archive-Market"))
                .andExpect(jsonPath("$.data.economy.totalRevenue").exists())
                .andExpect(jsonPath("$.data.economy.totalCost").exists())
                .andExpect(jsonPath("$.data.economy.profit").exists());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/operations/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.integration.nexus").value("DRY_RUN_CAPABLE"));
    }

    @Test
    void profitabilityEngineEvaluatesMarginsRiskDiscountsReviewEventsAndSummaries() throws Exception {
        var products = productService.seed();
        ProductEntity serviceContract = product(products, ProductType.SERVICE_CONTRACT);
        ProductEntity electronic = product(products, ProductType.ELECTRONIC_COMPONENT);
        ProductEntity battery = product(products, ProductType.BATTERY_MODULE);

        CustomerEntity vip = customerService.createSynthetic(2);
        MarketOrderEntity acceptedOrder = orderService.create(new CreateOrderRequest(
                vip.getCustomerId(), serviceContract.getProductId(), 1, false));
        OrderProfitabilityAssessmentEntity accepted = profitabilityService.get(acceptedOrder.getOrderId());
        assertThat(accepted.getRecommendation()).isEqualTo(ProfitabilityRecommendation.ACCEPT);
        assertThat(accepted.getMarginRate()).isGreaterThanOrEqualTo(new BigDecimal("15.0000"));
        assertThat(accepted.getRiskScore()).isLessThan(new BigDecimal("0.6000"));

        CustomerEntity retail = customerService.createSynthetic(1);
        MarketOrderEntity rejectedOrder = orderService.create(new CreateOrderRequest(
                retail.getCustomerId(), electronic.getProductId(), 1, false));
        OrderProfitabilityAssessmentEntity rejected = profitabilityService.get(rejectedOrder.getOrderId());
        assertThat(rejected.getRecommendation()).isEqualTo(ProfitabilityRecommendation.REJECT_RECOMMENDED);
        assertThat(rejected.getMarginRate()).isLessThan(new BigDecimal("5.0000"));

        CustomerEntity highRisk = customerService.createSynthetic(3);
        MarketOrderEntity highRiskOrder = orderService.create(new CreateOrderRequest(
                highRisk.getCustomerId(), serviceContract.getProductId(), 1, false));
        OrderProfitabilityAssessmentEntity highRiskAssessment = profitabilityService.get(highRiskOrder.getOrderId());
        assertThat(highRiskAssessment.getRecommendation()).isEqualTo(ProfitabilityRecommendation.REVIEW_REQUIRED);
        assertThat(highRiskAssessment.getRiskScore()).isGreaterThanOrEqualTo(new BigDecimal("0.7500"));

        CustomerEntity b2b = customerService.createSynthetic(0);
        MarketOrderEntity largeOrder = orderService.create(new CreateOrderRequest(
                b2b.getCustomerId(), battery.getProductId(), 3, false));
        OrderProfitabilityAssessmentEntity largeOrderAssessment = profitabilityService.get(largeOrder.getOrderId());
        assertThat(largeOrderAssessment.getRecommendation()).isEqualTo(ProfitabilityRecommendation.REVIEW_REQUIRED);
        assertThat(largeOrderAssessment.getOrderAmount()).isGreaterThanOrEqualTo(new BigDecimal("3000000.00"));

        CustomerEntity discountSeeker = customerService.createSynthetic(4);
        MarketOrderEntity discountedOrder = orderService.create(new CreateOrderRequest(
                discountSeeker.getCustomerId(), serviceContract.getProductId(), 1, false));
        OrderProfitabilityAssessmentEntity discounted = profitabilityService.get(discountedOrder.getOrderId());
        assertThat(discounted.getDiscountCost()).isEqualByComparingTo(discountedOrder.getDiscountAmount());
        assertThat(discounted.getExpectedReturnCost()).isGreaterThan(BigDecimal.ZERO);
        assertThat(discounted.getExpectedClaimCost()).isGreaterThan(BigDecimal.ZERO);
        assertThat(discounted.getExpectedTotalCost()).isEqualByComparingTo(
                discounted.getEstimatedProductionCost()
                        .add(discounted.getEstimatedLogisticsCost())
                        .add(discounted.getEstimatedLedgerFee())
                        .add(discounted.getPaymentProcessingFee())
                        .add(discounted.getDiscountCost())
                        .add(discounted.getExpectedReturnCost())
                        .add(discounted.getExpectedClaimCost())
                        .add(discounted.getCustomerAcquisitionCost())
                        .add(discounted.getMarketOperationCost()));

        assertThat(outboxRepository.findAll())
                .extracting("eventType")
                .contains("ORDER_REQUIRES_REVIEW");
        long beforeDuplicate = assessmentRepository.count();
        profitabilityService.evaluate(highRiskOrder.getOrderId());
        assertThat(assessmentRepository.count()).isEqualTo(beforeDuplicate);

        mockMvc.perform(get("/api/market-profitability/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluatedOrders").exists())
                .andExpect(jsonPath("$.data.reviewRequiredOrders").exists())
                .andExpect(jsonPath("$.data.expectedProfit").exists());

        mockMvc.perform(get("/api/market-economy/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profitability.evaluatedOrders").exists());
    }

    private ExternalEventRequest externalEvent(String eventId, String idempotencyKey, int hopCount, int maxHop) {
        return new ExternalEventRequest(
                eventId,
                idempotencyKey,
                "Archive-Nexus",
                "PRODUCTION_COMPLETED",
                1,
                "2026-07-10T00:00:00Z",
                "SIM-TEST",
                "SETTLEMENT-TEST",
                "CORR-TEST",
                "CAUSE-TEST",
                hopCount,
                maxHop,
                Map.of("orderId", "ORD-TEST"));
    }

    private ProductEntity product(List<ProductEntity> products, ProductType type) {
        return products.stream()
                .filter(product -> product.getProductType() == type)
                .findFirst()
                .orElseThrow();
    }
}
