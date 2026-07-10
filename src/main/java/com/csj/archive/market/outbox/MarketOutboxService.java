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
            return outboxRepository.findAll().stream()
                    .filter(event -> event.getIdempotencyKey().equals(idempotencyKey))
                    .findFirst()
                    .orElseThrow();
        }
        String eventId = IdGenerator.prefixed("EVT");
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId);
        envelope.put("idempotencyKey", idempotencyKey);
        envelope.put("source", "Archive-Market");
        envelope.put("eventType", eventType);
        envelope.put("schemaVersion", 1);
        envelope.put("occurredAt", Instant.now(clock).toString());
        envelope.put("simulationRunId", simulationRunId);
        envelope.put("settlementCycleId", settlementCycleId);
        envelope.put("correlationId", correlationId);
        envelope.put("causationId", causationId);
        envelope.put("hopCount", 1);
        envelope.put("maxHop", MAX_HOP);
        envelope.put("payload", payload);
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
}
