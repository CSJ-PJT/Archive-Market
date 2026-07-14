package com.csj.archive.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.csj.archive.market.capital.MarketCapitalService;
import com.csj.archive.market.capital.MarketWorkdaySnapshotRepository;
import com.csj.archive.market.capital.MarketWorkforceAllocationRepository;
import com.csj.archive.market.capital.WorkforceAllocationRequest;
import com.csj.archive.market.capital.WorkforceRole;
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
import com.csj.archive.market.outbox.MarketOutboxService;
import com.csj.archive.market.outbox.OutboxTargetService;
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
import com.csj.archive.market.profitability.ProfitabilityCostComponentAdjustmentRepository;
import com.csj.archive.market.profitability.ProfitabilityRecommendation;
import com.csj.archive.market.revenue.CostType;
import com.csj.archive.market.revenue.MarketCostEventRepository;
import com.csj.archive.market.revenue.MarketEconomyService;
import com.csj.archive.market.revenue.MarketProfitSnapshotRepository;
import com.csj.archive.market.revenue.MarketRevenueEventRepository;
import com.csj.archive.market.revenue.RevenueType;
import com.csj.archive.market.runtime.RuntimeAutoRunService;
import com.csj.archive.market.runtime.RuntimeEventService;
import com.csj.archive.market.simulation.MarketSimulationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
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
        registry.add("archive.runtime.autorun.enabled", () -> "true");
        registry.add("archive.runtime.autorun.scheduler-enabled", () -> "false");
        registry.add("archive.runtime.max-events-per-tick", () -> "2");
        registry.add("archive.runtime.max-backlog-per-tick", () -> "10000");
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
    @Autowired MarketCapitalService capitalService;
    @Autowired RuntimeAutoRunService runtimeAutoRunService;
    @Autowired RuntimeEventService runtimeEventService;
    @Autowired MarketOutboxPublisher outboxPublisher;
    @Autowired MarketOutboxService outboxService;
    @Autowired MarketOrderRepository orderRepository;
    @Autowired MarketPaymentRepository paymentRepository;
    @Autowired MarketRevenueEventRepository revenueRepository;
    @Autowired MarketCostEventRepository costRepository;
    @Autowired MarketOutboxRepository outboxRepository;
    @Autowired MarketInboxRepository inboxRepository;
    @Autowired MarketProfitSnapshotRepository snapshotRepository;
    @Autowired OrderProfitabilityAssessmentRepository assessmentRepository;
    @Autowired ProfitabilityCostComponentAdjustmentRepository costAdjustmentRepository;
    @Autowired MarketWorkdaySnapshotRepository workdaySnapshotRepository;
    @Autowired MarketWorkforceAllocationRepository workforceAllocationRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired MockMvc mockMvc;

    @Test
    void marketCommerceFlowUsesSyntheticEventsOutboxInboxAndSummaries() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/dashboard/"))
                .andExpect(status().isOk());

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
                .contains(CostType.DISCOUNT_COST_INCURRED, CostType.PAYMENT_PROCESSING_FEE_PAID,
                        CostType.PRODUCTION_PURCHASE_COST_INCURRED,
                        CostType.LOGISTICS_FULFILLMENT_FEE_INCURRED,
                        CostType.SETTLEMENT_AGENCY_FEE_INCURRED,
                        CostType.CONTROL_TOWER_FEE_INCURRED,
                        CostType.REFUND_RESERVE_BOOKED,
                        CostType.CLAIM_RESERVE_BOOKED);
        assertThat(outboxRepository.findAll())
                .extracting("eventType")
                .contains("PRODUCTION_PURCHASE_COST_INCURRED",
                        "LOGISTICS_FULFILLMENT_FEE_INCURRED",
                        "SETTLEMENT_AGENCY_FEE_INCURRED",
                        "CONTROL_TOWER_FEE_INCURRED");
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
                .andExpect(jsonPath("$.data.economy.gmv").exists())
                .andExpect(jsonPath("$.data.economy.recognizedRevenue").exists())
                .andExpect(jsonPath("$.data.economy.totalExpense").exists())
                .andExpect(jsonPath("$.data.economy.operatingProfit").exists())
                .andExpect(jsonPath("$.data.economy.operatingMargin").exists())
                .andExpect(jsonPath("$.data.economy.calculationScope").value("LIFETIME"))
                .andExpect(jsonPath("$.data.economy.periodStart").exists())
                .andExpect(jsonPath("$.data.economy.periodEnd").exists())
                .andExpect(jsonPath("$.data.economy.calculatedAt").exists())
                .andExpect(jsonPath("$.data.economy.dataAvailable").value(true))
                .andExpect(jsonPath("$.data.economy.workforceCost").exists())
                .andExpect(jsonPath("$.data.economy.productionPurchaseCost").exists())
                .andExpect(jsonPath("$.data.economy.logisticsFulfillmentCost").exists())
                .andExpect(jsonPath("$.data.economy.settlementAgencyFee").exists())
                .andExpect(jsonPath("$.data.economy.controlTowerFee").exists())
                .andExpect(jsonPath("$.data.economy.negativeProfitStreak").exists())
                .andExpect(jsonPath("$.data.economy.reserveBalance").exists())
                .andExpect(jsonPath("$.data.economy.outstandingPayables").exists())
                .andExpect(jsonPath("$.data.economy.cashDeltaReason").exists())
                .andExpect(jsonPath("$.data.economy.topRevenueDrivers").isArray())
                .andExpect(jsonPath("$.data.economy.topExpenseDrivers").isArray())
                .andExpect(jsonPath("$.data.economy.totalRevenue").exists())
                .andExpect(jsonPath("$.data.economy.totalCost").exists())
                .andExpect(jsonPath("$.data.economy.profit").exists());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/operations/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.integration.nexus").value("DRY_RUN_ONLY"))
                .andExpect(jsonPath("$.data.integration.externalWrite").value("EXTERNAL_WRITE_BLOCKED"))
                .andExpect(jsonPath("$.data.liveFlowAvailable").value(true));

        mockMvc.perform(get("/api/runtime-events/recent").param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sourceService").exists())
                .andExpect(jsonPath("$.data[0].eventType").exists())
                .andExpect(jsonPath("$.data[0].correlationId").exists())
                .andExpect(jsonPath("$.data[0].idempotencyKey").exists())
                .andExpect(jsonPath("$.data[0].cursor").exists());

        String latestCursor = runtimeEventService.recent(1).getFirst().cursor();
        long outboxBeforeCursorRead = outboxRepository.count();
        mockMvc.perform(get("/api/runtime-events/recent").param("after", latestCursor).param("limit", "20"))
                .andExpect(status().isOk());
        assertThat(outboxRepository.count()).isEqualTo(outboxBeforeCursorRead);

        mockMvc.perform(get("/api/runtime-events/correlation/CORR-TEST"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/runtime-events/entity/" + discountedOrder.getOrderId()))
                .andExpect(status().isOk());
        assertThat(runtimeEventService.byEntityId(discountedOrder.getOrderId()))
                .extracting("eventType")
                .contains("CUSTOMER_DEMAND_CREATED", "MARKET_ORDER_PLACED", "PAYMENT_CAPTURED",
                        "ORDER_PROFITABILITY_EVALUATED");
        assertThat(runtimeEventService.byEntityId(discountedOrder.getOrderId()).stream()
                .filter(event -> event.eventType().equals("PAYMENT_CAPTURED"))
                .count()).isEqualTo(1);

        mockMvc.perform(post("/api/simulations/orders").param("count", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/operations/summary")).andExpect(status().isOk());
        mockMvc.perform(get("/api/market-economy/summary")).andExpect(status().isOk());
    }

    @Test
    void newOrderKeepsOneRootCorrelationAcrossMarketEventsAndDownstreamPayloads() throws Exception {
        ProductEntity product = productService.seed().getFirst();
        CustomerEntity customer = customerService.createSynthetic(0);

        MarketOrderEntity order = orderService.create(new CreateOrderRequest(
                customer.getCustomerId(), product.getProductId(), 1, false));
        String rootCorrelationId = order.getRootCorrelationId();
        assertThat(rootCorrelationId).startsWith("CORR-");
        assertThat(order.getSimulationRunId()).startsWith("SIM-");
        assertThat(order.getCreatedEventIds()).isNotEmpty().doesNotHaveDuplicates();
        assertThat(order.getOutboxAcceptedCount()).isEqualTo(order.getCreatedEventIds().size());

        orderService.confirm(order.getOrderId());
        paymentService.capture(order.getOrderId());

        List<com.csj.archive.market.outbox.MarketOutboxEntity> orderEvents = outboxRepository.findAll().stream()
                .filter(event -> event.getAggregateId().equals(order.getOrderId()))
                .toList();
        assertThat(orderEvents).isNotEmpty();
        assertThat(orderEvents).extracting(com.csj.archive.market.outbox.MarketOutboxEntity::getEventId)
                .doesNotHaveDuplicates();
        assertThat(orderEvents).allMatch(com.csj.archive.market.outbox.MarketOutboxEntity::isPublishApproved);

        List<JsonNode> envelopes = orderEvents.stream()
                .map(event -> readJson(event.getPayload()))
                .toList();
        assertThat(envelopes).allSatisfy(envelope -> {
            assertThat(envelope.path("eventId").asText()).isNotBlank();
            assertThat(envelope.path("correlationId").asText()).isEqualTo(rootCorrelationId);
            assertThat(envelope.path("orderId").asText()).isEqualTo(order.getOrderId());
            assertThat(envelope.path("entityId").asText()).isEqualTo(order.getOrderId());
            assertThat(envelope.path("simulationRunId").asText()).isEqualTo(order.getSimulationRunId());
            assertThat(envelope.has("workdayId")).isTrue();
            assertThat(envelope.has("settlementCycleId")).isTrue();

            JsonNode payload = envelope.path("payload");
            assertThat(payload.path("eventId").asText()).isEqualTo(envelope.path("eventId").asText());
            assertThat(payload.path("correlationId").asText()).isEqualTo(rootCorrelationId);
            assertThat(payload.path("orderId").asText()).isEqualTo(order.getOrderId());
            assertThat(payload.path("entityId").asText()).isEqualTo(order.getOrderId());
            assertThat(payload.path("simulationRunId").asText()).isEqualTo(order.getSimulationRunId());
            assertThat(payload.has("workdayId")).isTrue();
            assertThat(payload.has("settlementCycleId")).isTrue();
        });

        String demandEventId = revenueRepository.findAll().stream()
                .filter(event -> event.getOrderId().equals(order.getOrderId()))
                .filter(event -> event.getRevenueType() == RevenueType.CUSTOMER_DEMAND_CREATED)
                .findFirst()
                .orElseThrow()
                .getEventId();
        String orderPlacedEventId = revenueRepository.findAll().stream()
                .filter(event -> event.getOrderId().equals(order.getOrderId()))
                .filter(event -> event.getRevenueType() == RevenueType.SALES_ORDER_PLACED)
                .findFirst()
                .orElseThrow()
                .getEventId();
        String confirmationEventId = revenueRepository.findAll().stream()
                .filter(event -> event.getOrderId().equals(order.getOrderId()))
                .filter(event -> event.getRevenueType() == RevenueType.SALES_ORDER_CONFIRMED)
                .findFirst()
                .orElseThrow()
                .getEventId();
        String paymentEventId = revenueRepository.findAll().stream()
                .filter(event -> event.getOrderId().equals(order.getOrderId()))
                .filter(event -> event.getRevenueType() == RevenueType.PAYMENT_CAPTURED)
                .findFirst()
                .orElseThrow()
                .getEventId();

        assertThat(envelopeFor(orderEvents, "MARKET_ORDER_PLACED").path("causationId").asText())
                .isEqualTo(demandEventId);
        assertThat(envelopeFor(orderEvents, "PRODUCTION_REQUESTED").path("causationId").asText())
                .isEqualTo(confirmationEventId);
        assertThat(envelopeFor(orderEvents, "SHIPMENT_REQUESTED").path("causationId").asText())
                .isEqualTo(confirmationEventId);
        assertThat(envelopeFor(orderEvents, "PAYMENT_CAPTURED").path("causationId").asText())
                .isEqualTo(paymentEventId);
        assertThat(envelopeFor(orderEvents, "SALES_REVENUE_CONFIRMED").path("causationId").asText())
                .isEqualTo(paymentEventId);

        assertThat(runtimeEventService.byEntityId(order.getOrderId()).stream()
                .filter(event -> event.eventType().equals("ORDER_PROFITABILITY_EVALUATED"))
                .findFirst()
                .orElseThrow()
                .causationId()).isEqualTo(orderPlacedEventId);

        assertThat(orderEvents.stream().filter(event -> event.getEventType().equals("PAYMENT_CAPTURED")).count())
                .isEqualTo(1);
        assertThat(revenueRepository.findAll().stream()
                .filter(event -> event.getOrderId().equals(order.getOrderId()))
                .filter(event -> event.getRevenueType() == RevenueType.PAYMENT_CAPTURED)
                .count()).isEqualTo(1);
        assertThat(orderEvents.stream()
                .filter(event -> event.getTargetService().name().equals("NEXUS"))
                .map(event -> readJson(event.getPayload()).path("correlationId").asText())
                .distinct()
                .toList()).containsExactly(rootCorrelationId);
        assertThat(runtimeEventService.byEntityId(order.getOrderId()))
                .extracting(event -> event.correlationId())
                .containsOnly(rootCorrelationId);
        assertThat(runtimeEventService.byEntityId(order.getOrderId()))
                .extracting(event -> event.eventId())
                .doesNotHaveDuplicates();
        assertThat(runtimeEventService.byEntityId(order.getOrderId()))
                .allSatisfy(event -> {
                    assertThat(event.orderId()).isEqualTo(order.getOrderId());
                    assertThat(event.sourceSystem()).isEqualTo("Archive-Market");
                });

        List<com.csj.archive.market.outbox.MarketOutboxEntity> archiveOsEvents = orderEvents.stream()
                .filter(event -> event.getTargetService() == OutboxTargetService.ARCHIVE_OS)
                .toList();
        assertThat(archiveOsEvents).extracting(com.csj.archive.market.outbox.MarketOutboxEntity::getEventType)
                .contains("CUSTOMER_DEMAND_CREATED", "SALES_ORDER_PLACED", "ORDER_PROFITABILITY_EVALUATED",
                        "SALES_ORDER_CONFIRMED", "PAYMENT_CAPTURED");
        assertThat(archiveOsEvents).extracting(com.csj.archive.market.outbox.MarketOutboxEntity::getEventId)
                .doesNotHaveDuplicates();
        assertThat(envelopeFor(archiveOsEvents, "CUSTOMER_DEMAND_CREATED").path("eventId").asText())
                .isEqualTo(demandEventId);
        assertThat(envelopeFor(archiveOsEvents, "SALES_ORDER_PLACED").path("eventId").asText())
                .isEqualTo(orderPlacedEventId);
        assertThat(envelopeFor(archiveOsEvents, "SALES_ORDER_CONFIRMED").path("eventId").asText())
                .isEqualTo(confirmationEventId);
        assertThat(envelopeFor(archiveOsEvents, "PAYMENT_CAPTURED").path("eventId").asText())
                .isEqualTo(paymentEventId);
        assertThat(archiveOsEvents).allSatisfy(event -> {
            JsonNode envelope = readJson(event.getPayload());
            assertThat(envelope.path("simulationRunId").asText()).isEqualTo(order.getSimulationRunId());
            assertThat(envelope.path("payload").path("simulationRunId").asText())
                    .isEqualTo(order.getSimulationRunId());
        });
        List<String> archiveOsEventIds = archiveOsEvents.stream()
                .map(com.csj.archive.market.outbox.MarketOutboxEntity::getEventId)
                .toList();
        assertThat(archiveOsEvents.stream()
                .map(event -> readJson(event.getPayload()).path("causationId").asText(null))
                .filter(java.util.Objects::nonNull)
                .toList())
                .allMatch(archiveOsEventIds::contains);
        assertThatThrownBy(() -> outboxService.create(OutboxTargetService.NEXUS, "MARKET_ORDER_PLACED",
                "MARKET_ORDER", order.getOrderId(), "SIM-RETRY", null, "CORR-OTHER", demandEventId,
                Map.of("orderId", order.getOrderId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different correlationId");
    }

    @Test
    void simulationRequestUsesOneRunIdAcrossOrdersEventsAndOutboxPayloads() {
        var result = simulationService.orders(2);
        assertThat(result.simulationRunId()).startsWith("SIM-");

        List<MarketOrderEntity> simulatedOrders = orderRepository.findAll().stream()
                .filter(order -> result.simulationRunId().equals(order.getSimulationRunId()))
                .toList();
        assertThat(simulatedOrders).hasSize(2);

        for (MarketOrderEntity order : simulatedOrders) {
            assertThat(revenueRepository.findAll().stream()
                    .filter(event -> order.getOrderId().equals(event.getOrderId()))
                    .map(event -> event.getSimulationRunId())
                    .distinct())
                    .containsExactly(result.simulationRunId());
            assertThat(costRepository.findAll().stream()
                    .filter(event -> order.getOrderId().equals(event.getOrderId()))
                    .map(event -> event.getSimulationRunId())
                    .distinct())
                    .containsExactly(result.simulationRunId());
            assertThat(outboxRepository.findAll().stream()
                    .filter(event -> order.getOrderId().equals(event.getAggregateId()))
                    .map(event -> readJson(event.getPayload()))
                    .map(envelope -> envelope.path("simulationRunId").asText())
                    .distinct())
                    .containsExactly(result.simulationRunId());
            assertThat(outboxRepository.findAll().stream()
                    .filter(event -> order.getOrderId().equals(event.getAggregateId()))
                    .map(event -> readJson(event.getPayload()).path("payload").path("simulationRunId").asText())
                    .distinct())
                    .containsExactly(result.simulationRunId());
            assertThat(runtimeEventService.byEntityId(order.getOrderId()).stream()
                    .map(event -> event.simulationRunId())
                    .distinct())
                    .containsExactly(result.simulationRunId());
        }
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
        inboxService.receive(measuredCostEvent("NEXUS-COST-1", "NEXUS:COST:1",
                "Archive-Nexus", "PRODUCTION_COMPLETED", acceptedOrder.getOrderId(),
                Map.of("orderId", acceptedOrder.getOrderId(), "actualProductionCost", 200000, "currency", "KRW")));
        OrderProfitabilityAssessmentEntity measured = profitabilityService.get(acceptedOrder.getOrderId());
        assertThat(measured.getEstimatedProductionCost()).isEqualByComparingTo("200000.00");
        assertThat(costAdjustmentRepository.findByOrderIdOrderByCreatedAtDesc(acceptedOrder.getOrderId()))
                .extracting("sourceService")
                .contains("Archive-Nexus");
        long beforeDuplicate = assessmentRepository.count();
        profitabilityService.evaluate(highRiskOrder.getOrderId());
        assertThat(assessmentRepository.count()).isEqualTo(beforeDuplicate);

        mockMvc.perform(get("/api/market-profitability/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluatedOrders").exists())
                .andExpect(jsonPath("$.data.reviewRequiredOrders").exists())
                .andExpect(jsonPath("$.data.measuredCostAdjustments").exists())
                .andExpect(jsonPath("$.data.expectedProfit").exists());

        mockMvc.perform(get("/api/market-economy/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profitability.evaluatedOrders").exists());
    }

    @Test
    void workingCapitalWorkforceProductivitySummariesAndWorkdaySimulation() throws Exception {
        simulationService.orders(5);
        capitalService.seedDefaults();
        capitalService.seedDefaults();
        assertThat(workforceAllocationRepository.countByWorkdayId(MarketCapitalService.DEFAULT_WORKDAY_ID))
                .isEqualTo(WorkforceRole.values().length);

        WorkforceAllocationRequest.RoleAllocation oneOperator =
                new WorkforceAllocationRequest.RoleAllocation(1, 1, BigDecimal.ZERO, BigDecimal.ONE);
        capitalService.allocate(new WorkforceAllocationRequest("WORKDAY-A",
                Map.of(WorkforceRole.ORDER_OPERATOR, oneOperator)));
        capitalService.allocate(new WorkforceAllocationRequest("WORKDAY-A",
                Map.of(WorkforceRole.ORDER_OPERATOR, new WorkforceAllocationRequest.RoleAllocation(2, 2, BigDecimal.ZERO, BigDecimal.ONE))));
        capitalService.allocate(new WorkforceAllocationRequest("WORKDAY-B",
                Map.of(WorkforceRole.ORDER_OPERATOR, oneOperator)));
        assertThat(workforceAllocationRepository.countByWorkdayId("WORKDAY-A")).isEqualTo(1);
        assertThat(workforceAllocationRepository.countByWorkdayId("WORKDAY-B")).isEqualTo(1);

        long beforeSummaryCount = workforceAllocationRepository.count();
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/operations/summary")).andExpect(status().isOk());
            mockMvc.perform(get("/api/market-economy/summary")).andExpect(status().isOk());
        }
        assertThat(workforceAllocationRepository.count()).isEqualTo(beforeSummaryCount);

        EnumMap<WorkforceRole, WorkforceAllocationRequest.RoleAllocation> allocations = new EnumMap<>(WorkforceRole.class);
        for (WorkforceRole role : WorkforceRole.values()) {
            allocations.put(role, new WorkforceAllocationRequest.RoleAllocation(0, 0, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        capitalService.allocate(new WorkforceAllocationRequest(allocations));

        Map<String, Object> cashflow = capitalService.cashflowSummary();
        assertThat(cashflow).containsKeys("availableCash", "expectedReceivable", "pendingSettlementAmount",
                "payrollCost", "productionRequestCost", "logisticsRequestCost", "ledgerFee", "netProfit",
                "workingCapital");

        Map<String, Object> workforce = capitalService.workforceSummary();
        assertThat(workforce).containsKeys("roles", "processingCapacity", "backlog", "payrollCost");
        assertThat(((Number) workforce.get("processingCapacity")).longValue()).isGreaterThanOrEqualTo(0);

        Map<String, Object> productivity = capitalService.productivitySummary();
        assertThat(productivity).containsKeys("productivityScore", "revenueConversion", "cancellationRate",
                "claimRate", "delayRisk", "aiAgentRecommendation");

        simulationService.runWorkday(LocalDate.of(2026, 7, 10));
        assertThat(workdaySnapshotRepository.findTopByOrderByWorkDateDescCreatedAtDesc()).isPresent();
        assertThat(runtimeEventService.recent(300))
                .extracting("eventType")
                .contains("WORKDAY_COMPLETED", "CAPACITY_SHORTAGE_DETECTED", "BACKLOG_INCREASED");

        mockMvc.perform(get("/api/market-cashflow/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableCash").exists());
        mockMvc.perform(get("/api/market-workforce/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processingCapacity").exists());
        mockMvc.perform(get("/api/market-productivity/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aiAgentRecommendation").exists());
        mockMvc.perform(get("/api/workforce/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.effectiveCapacity").exists())
                .andExpect(jsonPath("$.data.usedCapacity").exists());
        mockMvc.perform(get("/api/productivity/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productivityScore").exists());
        mockMvc.perform(get("/api/capacity/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.effectiveCapacity").exists())
                .andExpect(jsonPath("$.data.backlog").exists());
        mockMvc.perform(get("/api/operations/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cashflow.availableCash").exists())
                .andExpect(jsonPath("$.data.workforce.processingCapacity").exists())
                .andExpect(jsonPath("$.data.productivity.productivityScore").exists());
    }

    @Test
    void autonomousRuntimeWorkLoopProducesBoundedIdempotentWorkAndStatus() throws Exception {
        long ordersBefore = orderRepository.count();
        long outboxBefore = outboxRepository.count();

        var firstTick = runtimeAutoRunService.runTick();
        assertThat(firstTick.autoRunEnabled()).isTrue();
        assertThat(firstTick.eventsProducedLastTick()).isBetween(1, 2);
        assertThat(firstTick.lastWorkAt()).isNotNull();
        assertThat(firstTick.lastEventAt()).isNotNull();
        assertThat(orderRepository.count()).isGreaterThan(ordersBefore);
        assertThat(outboxRepository.count()).isGreaterThan(outboxBefore);
        MarketOrderEntity autoRunOrder = orderRepository.findAll().stream()
                .max(java.util.Comparator.comparing(MarketOrderEntity::getId))
                .orElseThrow();
        OrderProfitabilityAssessmentEntity autoRunAssessment = profitabilityService.get(autoRunOrder.getOrderId());
        if (autoRunAssessment.getRecommendation() == ProfitabilityRecommendation.ACCEPT) {
            assertThat(paymentRepository.findByOrderId(autoRunOrder.getOrderId()))
                    .isPresent()
                    .get()
                    .extracting("paymentStatus")
                    .isEqualTo(PaymentStatus.CAPTURED);
        } else {
            assertThat(paymentRepository.findByOrderId(autoRunOrder.getOrderId())).isEmpty();
        }

        long ordersAfterFirstTick = orderRepository.count();
        var duplicateTick = runtimeAutoRunService.runTick();
        assertThat(duplicateTick.eventsProducedLastTick()).isZero();
        assertThat(orderRepository.count()).isEqualTo(ordersAfterFirstTick);

        long ordersBeforeSummary = orderRepository.count();
        mockMvc.perform(get("/api/operations/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime.lastWorkAt").exists())
                .andExpect(jsonPath("$.data.runtime.lastEventAt").exists());
        mockMvc.perform(get("/api/runtime/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastWorkAt").exists())
                .andExpect(jsonPath("$.data.lastEventAt").exists())
                .andExpect(jsonPath("$.data.latestCursor").exists())
                .andExpect(jsonPath("$.data.oldestBacklogAgeSeconds").exists());
        assertThat(orderRepository.count()).isEqualTo(ordersBeforeSummary);
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

    private ExternalEventRequest measuredCostEvent(String eventId, String idempotencyKey, String source,
                                                   String eventType, String orderId, Map<String, Object> payload) {
        return new ExternalEventRequest(
                eventId,
                idempotencyKey,
                source,
                eventType,
                1,
                "2026-07-10T00:00:00Z",
                "SIM-COST-TEST",
                "SETTLEMENT-COST-TEST",
                "CORR-COST-TEST",
                orderId,
                1,
                5,
                payload);
    }

    private ProductEntity product(List<ProductEntity> products, ProductType type) {
        return products.stream()
                .filter(product -> product.getProductType() == type)
                .findFirst()
                .orElseThrow();
    }

    private JsonNode envelopeFor(List<com.csj.archive.market.outbox.MarketOutboxEntity> events, String eventType) {
        return events.stream()
                .filter(event -> event.getEventType().equals(eventType))
                .findFirst()
                .map(event -> readJson(event.getPayload()))
                .orElseThrow();
    }

    private JsonNode readJson(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new AssertionError("Invalid outbox JSON", ex);
        }
    }
}
