package com.csj.archive.market.inbox;

import com.csj.archive.market.common.BusinessException;
import com.csj.archive.market.profitability.ExternalCostComponentAdapter;
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
public class MarketInboxService {

    private final MarketInboxRepository inboxRepository;
    private final ExternalCostComponentAdapter costComponentAdapter;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public MarketInboxService(MarketInboxRepository inboxRepository, ExternalCostComponentAdapter costComponentAdapter,
                              ObjectMapper objectMapper, Clock clock) {
        this.inboxRepository = inboxRepository;
        this.costComponentAdapter = costComponentAdapter;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public MarketInboxEntity receive(ExternalEventRequest request) {
        Instant now = Instant.now(clock);
        if (request.hopCount() > request.maxHop()) {
            MarketInboxEntity rejected = new MarketInboxEntity(
                    request.eventId(),
                    request.idempotencyKey(),
                    request.source(),
                    request.eventType(),
                    json(request),
                    MarketInboxStatus.REJECTED,
                    now,
                    now,
                    "hopCount exceeds maxHop");
            return inboxRepository.save(rejected);
        }
        if (inboxRepository.existsByEventIdOrIdempotencyKey(request.eventId(), request.idempotencyKey())) {
            throw new BusinessException("DUPLICATE_EXTERNAL_EVENT", "External event already received");
        }
        MarketInboxEntity saved = inboxRepository.save(new MarketInboxEntity(
                request.eventId(),
                request.idempotencyKey(),
                request.source(),
                request.eventType(),
                json(request),
                MarketInboxStatus.PROCESSED,
                now,
                now,
                null));
        costComponentAdapter.adapt(request);
        return saved;
    }

    @Transactional
    public List<MarketInboxEntity> receiveBulk(List<ExternalEventRequest> requests) {
        return requests.stream().map(this::receive).toList();
    }

    @Transactional(readOnly = true)
    public List<MarketInboxEntity> list() {
        return inboxRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> summary() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (MarketInboxStatus status : MarketInboxStatus.values()) {
            result.put(status.name().toLowerCase(), inboxRepository.countByStatus(status));
        }
        return result;
    }

    private String json(Object request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
