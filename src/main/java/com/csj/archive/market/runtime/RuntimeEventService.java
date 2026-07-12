package com.csj.archive.market.runtime;

import com.csj.archive.market.capital.MarketWorkdaySnapshotEntity;
import com.csj.archive.market.capital.MarketWorkdaySnapshotRepository;
import com.csj.archive.market.inbox.MarketInboxEntity;
import com.csj.archive.market.inbox.MarketInboxRepository;
import com.csj.archive.market.inbox.MarketInboxStatus;
import com.csj.archive.market.outbox.MarketOutboxEntity;
import com.csj.archive.market.outbox.MarketOutboxRepository;
import com.csj.archive.market.outbox.OutboxStatus;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.profitability.OrderProfitabilityAssessmentEntity;
import com.csj.archive.market.profitability.OrderProfitabilityAssessmentRepository;
import com.csj.archive.market.profitability.ProfitabilityRecommendation;
import com.csj.archive.market.revenue.MarketRevenueEventEntity;
import com.csj.archive.market.revenue.MarketRevenueEventRepository;
import com.csj.archive.market.revenue.RevenueType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuntimeEventService {

    private static final String SOURCE_SERVICE = "Archive-Market";
    private static final String DOMAIN = "market";

    private final MarketOutboxRepository outboxRepository;
    private final MarketInboxRepository inboxRepository;
    private final MarketRevenueEventRepository revenueRepository;
    private final OrderProfitabilityAssessmentRepository assessmentRepository;
    private final MarketWorkdaySnapshotRepository workdaySnapshotRepository;
    private final MarketOrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public RuntimeEventService(MarketOutboxRepository outboxRepository, MarketInboxRepository inboxRepository,
                               MarketRevenueEventRepository revenueRepository,
                               OrderProfitabilityAssessmentRepository assessmentRepository,
                               MarketWorkdaySnapshotRepository workdaySnapshotRepository,
                               MarketOrderRepository orderRepository,
                               ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.inboxRepository = inboxRepository;
        this.revenueRepository = revenueRepository;
        this.assessmentRepository = assessmentRepository;
        this.workdaySnapshotRepository = workdaySnapshotRepository;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<RuntimeEventResponse> recent(int limit) {
        return recent(null, limit);
    }

    @Transactional(readOnly = true)
    public List<RuntimeEventResponse> recent(String after, int limit) {
        int boundedLimit = normalizeLimit(limit);
        if (after != null && !after.isBlank()) {
            Cursor cursor = decodeCursor(after);
            return allMapped().stream()
                    .filter(event -> isAfter(event, cursor))
                    .sorted(Comparator.comparing(RuntimeEventResponse::occurredAt,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(RuntimeEventResponse::eventId, Comparator.nullsLast(Comparator.naturalOrder())))
                    .limit(boundedLimit)
                    .toList();
        }
        List<RuntimeEventResponse> events = new ArrayList<>();
        events.addAll(outboxRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .map(this::fromOutbox)
                .toList());
        events.addAll(inboxRepository.findByOrderByReceivedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .map(this::fromInbox)
                .toList());
        events.addAll(revenueRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .filter(this::isProjectedRevenueEvent)
                .map(this::fromRevenue)
                .toList());
        events.addAll(assessmentRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .map(this::fromAssessment)
                .toList());
        events.addAll(workdaySnapshotRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .flatMap(snapshot -> fromWorkday(snapshot).stream())
                .toList());
        return events.stream()
                .sorted(Comparator.comparing(RuntimeEventResponse::occurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(boundedLimit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RuntimeEventResponse> byCorrelationId(String correlationId) {
        return allMapped().stream()
                .filter(event -> Objects.equals(event.correlationId(), correlationId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RuntimeEventResponse> byEntityId(String entityId) {
        return allMapped().stream()
                .filter(event -> Objects.equals(event.entityId(), entityId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Instant> latestEventAt() {
        return recent(1).stream().findFirst().map(RuntimeEventResponse::occurredAt);
    }

    @Transactional(readOnly = true)
    public String latestCursor() {
        return recent(1).stream().findFirst().map(RuntimeEventResponse::cursor).orElse(null);
    }

    private List<RuntimeEventResponse> allMapped() {
        List<RuntimeEventResponse> events = new ArrayList<>();
        events.addAll(outboxRepository.findAll().stream().map(this::fromOutbox).toList());
        events.addAll(inboxRepository.findAll().stream().map(this::fromInbox).toList());
        events.addAll(revenueRepository.findAll().stream()
                .filter(this::isProjectedRevenueEvent)
                .map(this::fromRevenue)
                .toList());
        events.addAll(assessmentRepository.findAll().stream().map(this::fromAssessment).toList());
        events.addAll(workdaySnapshotRepository.findAll().stream()
                .flatMap(snapshot -> fromWorkday(snapshot).stream())
                .toList());
        return events.stream()
                .sorted(Comparator.comparing(RuntimeEventResponse::occurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private RuntimeEventResponse fromOutbox(MarketOutboxEntity event) {
        Map<String, Object> envelope = parse(event.getPayload());
        String correlationId = stringValue(envelope.get("correlationId"));
        String causationId = stringValue(envelope.get("causationId"));
        Instant occurredAt = instantValue(envelope.get("occurredAt")).orElse(event.getCreatedAt());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("direction", "outbound");
        metadata.put("targetService", event.getTargetService().name());
        metadata.put("idempotencyKey", event.getIdempotencyKey());
        metadata.put("aggregateType", event.getAggregateType());
        metadata.put("retryCount", event.getRetryCount());
        return new RuntimeEventResponse(
                event.getEventId(),
                event.getIdempotencyKey(),
                SOURCE_SERVICE,
                event.getTargetService().name(),
                DOMAIN,
                event.getEventType(),
                entityType(event.getAggregateType()),
                event.getAggregateId(),
                correlationId,
                causationId,
                stringValue(envelope.get("simulationRunId")),
                stringValue(envelope.get("settlementCycleId")),
                stringValue(envelope.get("workdayId")),
                status(event.getStatus(), event.getEventType()),
                severity(event.getStatus()),
                displayLabel("outbound", event.getEventType(), event.getAggregateId()),
                occurredAt,
                intValue(envelope.get("hopCount"), 1),
                intValue(envelope.get("maxHop"), 5),
                cursor(occurredAt, event.getEventId()),
                metadata);
    }

    private RuntimeEventResponse fromRevenue(MarketRevenueEventEntity event) {
        String entityId = firstNonBlank(event.getOrderId(), event.getEventId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("direction", "internal");
        metadata.put("idempotencyKey", event.getIdempotencyKey());
        metadata.put("simulationRunId", event.getSimulationRunId());
        metadata.put("settlementCycleId", event.getSettlementCycleId());
        metadata.put("currency", event.getCurrency());
        metadata.put("amount", event.getRevenueAmount());
        return new RuntimeEventResponse(
                event.getEventId(),
                event.getIdempotencyKey(),
                SOURCE_SERVICE,
                null,
                DOMAIN,
                event.getRevenueType().name(),
                entityType(null, event.getRevenueType().name()),
                entityId,
                rootCorrelationId(entityId),
                null,
                event.getSimulationRunId(),
                event.getSettlementCycleId(),
                null,
                "CREATED",
                "INFO",
                SOURCE_SERVICE + " internal " + event.getRevenueType().name() + " for " + entityId,
                event.getCreatedAt(),
                0,
                0,
                cursor(event.getCreatedAt(), event.getEventId()),
                metadata);
    }

    private RuntimeEventResponse fromAssessment(OrderProfitabilityAssessmentEntity assessment) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("direction", "internal");
        metadata.put("assessmentId", assessment.getAssessmentId());
        metadata.put("customerId", assessment.getCustomerId());
        metadata.put("customerType", assessment.getCustomerType().name());
        metadata.put("recommendation", assessment.getRecommendation().name());
        metadata.put("marginRate", assessment.getMarginRate());
        metadata.put("riskScore", assessment.getRiskScore());
        metadata.put("expectedProfit", assessment.getExpectedProfit());
        return new RuntimeEventResponse(
                "RTE-" + assessment.getAssessmentId(),
                "MARKET:ORDER_PROFITABILITY_EVALUATED:" + assessment.getOrderId(),
                SOURCE_SERVICE,
                null,
                DOMAIN,
                "ORDER_PROFITABILITY_EVALUATED",
                "order",
                assessment.getOrderId(),
                rootCorrelationId(assessment.getOrderId()),
                assessment.getCausationEventId(),
                null,
                null,
                null,
                assessmentStatus(assessment),
                assessmentSeverity(assessment),
                SOURCE_SERVICE + " evaluated profitability for " + assessment.getOrderId(),
                assessment.getCreatedAt(),
                0,
                0,
                cursor(assessment.getCreatedAt(), "RTE-" + assessment.getAssessmentId()),
                metadata);
    }

    private List<RuntimeEventResponse> fromWorkday(MarketWorkdaySnapshotEntity snapshot) {
        List<RuntimeEventResponse> events = new ArrayList<>();
        events.add(workdayEvent(snapshot, "WORKDAY_COMPLETED", "COMPLETED", "NORMAL"));
        if (snapshot.getBacklogCount() > 0) {
            events.add(workdayEvent(snapshot, "CAPACITY_SHORTAGE_DETECTED", "DELAYED", "WARNING"));
            events.add(workdayEvent(snapshot, "BACKLOG_INCREASED", "DELAYED", "WARNING"));
        }
        return events;
    }

    private RuntimeEventResponse workdayEvent(MarketWorkdaySnapshotEntity snapshot, String eventType,
                                              String status, String severity) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("direction", "internal");
        metadata.put("workDate", snapshot.getWorkDate());
        metadata.put("orderCount", snapshot.getOrderCount());
        metadata.put("processingCapacity", snapshot.getProcessingCapacity());
        metadata.put("backlog", snapshot.getBacklogCount());
        metadata.put("availableCash", snapshot.getAvailableCash());
        metadata.put("workingCapital", snapshot.getWorkingCapital());
        metadata.put("productivityScore", snapshot.getProductivityScore());
        return new RuntimeEventResponse(
                "RTE-" + eventType + "-" + snapshot.getSnapshotId(),
                "MARKET:" + eventType + ":" + snapshot.getSnapshotId(),
                SOURCE_SERVICE,
                null,
                DOMAIN,
                eventType,
                "workday",
                snapshot.getSnapshotId(),
                correlationId(snapshot.getSnapshotId()),
                snapshot.getSnapshotId(),
                null,
                null,
                snapshot.getSnapshotId(),
                status,
                severity,
                SOURCE_SERVICE + " " + eventType + " for " + snapshot.getSnapshotId(),
                snapshot.getCreatedAt(),
                0,
                0,
                cursor(snapshot.getCreatedAt(), "RTE-" + eventType + "-" + snapshot.getSnapshotId()),
                metadata);
    }

    private RuntimeEventResponse fromInbox(MarketInboxEntity event) {
        Map<String, Object> envelope = parse(event.getPayload());
        Map<String, Object> payload = nestedMap(envelope.get("payload"));
        String entityId = firstNonBlank(
                stringValue(payload.get("orderId")),
                stringValue(payload.get("entityId")),
                stringValue(envelope.get("causationId")),
                event.getEventId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("direction", "inbound");
        metadata.put("idempotencyKey", event.getIdempotencyKey());
        metadata.put("processedAt", event.getProcessedAt());
        return new RuntimeEventResponse(
                event.getEventId(),
                event.getIdempotencyKey(),
                event.getSourceService(),
                SOURCE_SERVICE,
                DOMAIN,
                event.getEventType(),
                entityType(stringValue(payload.get("entityType")), event.getEventType()),
                entityId,
                stringValue(envelope.get("correlationId")),
                stringValue(envelope.get("causationId")),
                stringValue(envelope.get("simulationRunId")),
                stringValue(envelope.get("settlementCycleId")),
                stringValue(envelope.get("workdayId")),
                status(event.getStatus()),
                severity(event.getStatus()),
                displayLabel("inbound", event.getEventType(), entityId),
                instantValue(envelope.get("occurredAt")).orElse(event.getReceivedAt()),
                intValue(envelope.get("hopCount"), 1),
                intValue(envelope.get("maxHop"), 5),
                cursor(instantValue(envelope.get("occurredAt")).orElse(event.getReceivedAt()), event.getEventId()),
                metadata);
    }

    private Map<String, Object> parse(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String entityType(String aggregateType) {
        if (aggregateType == null || aggregateType.isBlank()) {
            return "event";
        }
        return aggregateType.toLowerCase(Locale.ROOT).replace("market_", "").replace('_', '-');
    }

    private String entityType(String explicitType, String eventType) {
        if (explicitType != null && !explicitType.isBlank()) {
            return explicitType;
        }
        if (eventType != null && eventType.contains("ORDER")) {
            return "order";
        }
        if (eventType != null && eventType.contains("PAYMENT")) {
            return "payment";
        }
        if (eventType != null && eventType.contains("CLAIM")) {
            return "claim";
        }
        if (eventType != null && eventType.contains("DEMAND")) {
            return "demand";
        }
        return "event";
    }

    private boolean isProjectedRevenueEvent(MarketRevenueEventEntity event) {
        // PAYMENT_CAPTURED is projected from the Ledger outbox envelope so one order has one public payment event.
        return event.getRevenueType() == RevenueType.CUSTOMER_DEMAND_CREATED;
    }

    private String assessmentStatus(OrderProfitabilityAssessmentEntity assessment) {
        if (assessment.getRecommendation() == ProfitabilityRecommendation.REVIEW_REQUIRED) {
            return "WAITING";
        }
        if (assessment.getRecommendation() == ProfitabilityRecommendation.REJECT_RECOMMENDED) {
            return "FAILED";
        }
        return "COMPLETED";
    }

    private String assessmentSeverity(OrderProfitabilityAssessmentEntity assessment) {
        if (assessment.getRecommendation() == ProfitabilityRecommendation.REJECT_RECOMMENDED) {
            return "CRITICAL";
        }
        if (assessment.getRecommendation() == ProfitabilityRecommendation.REVIEW_REQUIRED) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private String correlationId(String entityId) {
        return entityId == null ? null : "CORR-" + entityId;
    }

    private String rootCorrelationId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(order -> order.getRootCorrelationId() == null || order.getRootCorrelationId().isBlank()
                        ? correlationId(orderId)
                        : order.getRootCorrelationId())
                .orElse(correlationId(orderId));
    }

    private String status(OutboxStatus status, String eventType) {
        if ("ORDER_REQUIRES_REVIEW".equals(eventType)
                || "LOW_MARGIN_ORDER_DETECTED".equals(eventType)
                || "HIGH_RISK_ORDER_DETECTED".equals(eventType)) {
            return "WAITING";
        }
        return switch (status) {
            case PENDING -> "WAITING";
            case PUBLISHED, DRY_RUN -> "COMPLETED";
            case RETRY -> "DELAYED";
            case FAILED -> "FAILED";
            case SKIPPED -> "FAILED";
        };
    }

    private String status(MarketInboxStatus status) {
        return switch (status) {
            case RECEIVED -> "WAITING";
            case PROCESSED, DUPLICATE -> "COMPLETED";
            case REJECTED -> "FAILED";
            case FAILED -> "FAILED";
        };
    }

    private String severity(OutboxStatus status) {
        return switch (status) {
            case FAILED -> "CRITICAL";
            case RETRY, SKIPPED -> "WARNING";
            case PENDING -> "INFO";
            case PUBLISHED, DRY_RUN -> "NORMAL";
        };
    }

    private String severity(MarketInboxStatus status) {
        return switch (status) {
            case FAILED, REJECTED -> "WARNING";
            case RECEIVED -> "INFO";
            case PROCESSED, DUPLICATE -> "NORMAL";
        };
    }

    private String displayLabel(String direction, String eventType, String entityId) {
        return SOURCE_SERVICE + " " + direction + " " + eventType + " for " + entityId;
    }

    private Optional<Instant> instantValue(Object value) {
        try {
            return value == null ? Optional.empty() : Optional.of(Instant.parse(value.toString()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 100;
        }
        return Math.min(limit, 500);
    }

    private boolean isAfter(RuntimeEventResponse event, Cursor cursor) {
        if (event.occurredAt() == null || cursor == null) {
            return false;
        }
        int timeComparison = event.occurredAt().compareTo(cursor.occurredAt());
        return timeComparison > 0 || (timeComparison == 0 && event.eventId().compareTo(cursor.eventId()) > 0);
    }

    private String cursor(Instant occurredAt, String eventId) {
        if (occurredAt == null || eventId == null) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                (occurredAt.toEpochMilli() + "|" + eventId).getBytes(StandardCharsets.UTF_8));
    }

    private Cursor decodeCursor(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 2);
            return new Cursor(Instant.ofEpochMilli(Long.parseLong(parts[0])), parts[1]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid runtime event cursor");
        }
    }

    private int intValue(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private record Cursor(Instant occurredAt, String eventId) {
    }
}
