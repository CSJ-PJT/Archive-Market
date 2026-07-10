package com.csj.archive.market.runtime;

import com.csj.archive.market.inbox.MarketInboxEntity;
import com.csj.archive.market.inbox.MarketInboxRepository;
import com.csj.archive.market.inbox.MarketInboxStatus;
import com.csj.archive.market.outbox.MarketOutboxEntity;
import com.csj.archive.market.outbox.MarketOutboxRepository;
import com.csj.archive.market.outbox.OutboxStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
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
    private final ObjectMapper objectMapper;

    public RuntimeEventService(MarketOutboxRepository outboxRepository, MarketInboxRepository inboxRepository,
                               ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.inboxRepository = inboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<RuntimeEventResponse> recent(int limit) {
        int boundedLimit = normalizeLimit(limit);
        List<RuntimeEventResponse> events = new ArrayList<>();
        events.addAll(outboxRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .map(this::fromOutbox)
                .toList());
        events.addAll(inboxRepository.findByOrderByReceivedAtDesc(PageRequest.of(0, boundedLimit)).stream()
                .map(this::fromInbox)
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

    private List<RuntimeEventResponse> allMapped() {
        List<RuntimeEventResponse> events = new ArrayList<>();
        events.addAll(outboxRepository.findAll().stream().map(this::fromOutbox).toList());
        events.addAll(inboxRepository.findAll().stream().map(this::fromInbox).toList());
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
                SOURCE_SERVICE,
                DOMAIN,
                event.getEventType(),
                entityType(event.getAggregateType()),
                event.getAggregateId(),
                correlationId,
                causationId,
                status(event.getStatus(), event.getEventType()),
                severity(event.getStatus()),
                displayLabel("outbound", event.getEventType(), event.getAggregateId()),
                occurredAt,
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
                event.getSourceService(),
                DOMAIN,
                event.getEventType(),
                entityType(stringValue(payload.get("entityType")), event.getEventType()),
                entityId,
                stringValue(envelope.get("correlationId")),
                stringValue(envelope.get("causationId")),
                status(event.getStatus()),
                severity(event.getStatus()),
                displayLabel("inbound", event.getEventType(), entityId),
                instantValue(envelope.get("occurredAt")).orElse(event.getReceivedAt()),
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
        return "event";
    }

    private String status(OutboxStatus status, String eventType) {
        if ("ORDER_REQUIRES_REVIEW".equals(eventType)
                || "LOW_MARGIN_ORDER_DETECTED".equals(eventType)
                || "HIGH_RISK_ORDER_DETECTED".equals(eventType)) {
            return "approval_required";
        }
        return switch (status) {
            case PENDING -> "waiting";
            case PUBLISHED, DRY_RUN -> "completed";
            case RETRY -> "delayed";
            case FAILED -> "failed";
            case SKIPPED -> "unavailable";
        };
    }

    private String status(MarketInboxStatus status) {
        return switch (status) {
            case RECEIVED -> "waiting";
            case PROCESSED, DUPLICATE -> "completed";
            case REJECTED -> "rejected";
            case FAILED -> "failed";
        };
    }

    private String severity(OutboxStatus status) {
        return switch (status) {
            case FAILED -> "critical";
            case RETRY, SKIPPED -> "warning";
            case PENDING -> "info";
            case PUBLISHED, DRY_RUN -> "normal";
        };
    }

    private String severity(MarketInboxStatus status) {
        return switch (status) {
            case FAILED, REJECTED -> "warning";
            case RECEIVED -> "info";
            case PROCESSED, DUPLICATE -> "normal";
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
}
