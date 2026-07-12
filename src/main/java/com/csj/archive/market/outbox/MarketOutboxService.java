package com.csj.archive.market.outbox;

import com.csj.archive.market.common.IdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketOutboxService {

    private static final int MAX_HOP = 5;

    private final MarketOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public MarketOutboxService(MarketOutboxRepository outboxRepository, ObjectMapper objectMapper, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public MarketOutboxEntity create(OutboxTargetService target, String eventType, String aggregateType,
                                     String aggregateId, String simulationRunId, String settlementCycleId,
                                     String correlationId, String causationId, Map<String, Object> payload) {
        String idempotencyKey = "MARKET:" + eventType + ":" + aggregateId;
        if (outboxRepository.existsByIdempotencyKey(idempotencyKey)) {
            MarketOutboxEntity existing = outboxRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
            String existingCorrelationId = correlationId(existing.getPayload());
            if (existingCorrelationId != null && !existingCorrelationId.equals(correlationId)) {
                throw new IllegalStateException("Outbox idempotency key is already bound to a different correlationId");
            }
            return existing;
        }
        String eventId = IdGenerator.prefixed("EVT");
        Map<String, Object> downstreamPayload = new LinkedHashMap<>(payload == null ? Map.of() : payload);
        String workdayId = value(downstreamPayload.get("workdayId"));
        downstreamPayload.put("eventId", eventId);
        downstreamPayload.put("correlationId", correlationId);
        downstreamPayload.put("causationId", causationId);
        downstreamPayload.put("orderId", downstreamPayload.getOrDefault("orderId", aggregateId));
        downstreamPayload.put("entityId", downstreamPayload.getOrDefault("entityId", aggregateId));
        downstreamPayload.put("simulationRunId", simulationRunId);
        downstreamPayload.put("workdayId", workdayId);
        downstreamPayload.put("settlementCycleId", settlementCycleId);
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId);
        envelope.put("idempotencyKey", idempotencyKey);
        envelope.put("source", "Archive-Market");
        envelope.put("sourceService", "Archive-Market");
        envelope.put("targetService", target.name());
        envelope.put("eventType", eventType);
        envelope.put("orderId", aggregateId);
        envelope.put("entityId", aggregateId);
        envelope.put("schemaVersion", 1);
        envelope.put("occurredAt", Instant.now(clock).toString());
        envelope.put("simulationRunId", simulationRunId);
        envelope.put("settlementCycleId", settlementCycleId);
        envelope.put("workdayId", workdayId);
        envelope.put("correlationId", correlationId);
        envelope.put("causationId", causationId);
        envelope.put("hopCount", 1);
        envelope.put("maxHop", MAX_HOP);
        envelope.put("payload", downstreamPayload);
        return outboxRepository.save(new MarketOutboxEntity(
                eventId, idempotencyKey, target, eventType, aggregateType, aggregateId, json(envelope)));
    }

    @Transactional(readOnly = true)
    public List<MarketOutboxEntity> list() {
        return outboxRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> summary() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (OutboxStatus status : OutboxStatus.values()) {
            result.put(status.name().toLowerCase(), outboxRepository.countByStatus(status));
        }
        return result;
    }

    @Transactional
    public List<MarketOutboxEntity> markFailedForRetry() {
        List<MarketOutboxEntity> failed = outboxRepository.findTop100ByStatusInOrderByCreatedAtAsc(List.of(OutboxStatus.FAILED, OutboxStatus.RETRY));
        failed.forEach(event -> event.markFailed("manual retry requested", Instant.now(clock)));
        return failed;
    }

    private String json(Map<String, Object> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private String correlationId(String payload) {
        try {
            Map<String, Object> envelope = objectMapper.readValue(payload, LinkedHashMap.class);
            return value(envelope.get("correlationId"));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot read persisted outbox payload", ex);
        }
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }
}
