package com.csj.archive.market.outbox;

import com.csj.archive.market.integration.archiveos.ArchiveOsClient;
import com.csj.archive.market.integration.archiveos.ArchiveOsPublishException;
import com.csj.archive.market.integration.ledger.LedgerClient;
import com.csj.archive.market.integration.nexus.NexusClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketOutboxPublisher {

    private final MarketOutboxRepository outboxRepository;
    private final NexusClient nexusClient;
    private final LedgerClient ledgerClient;
    private final ArchiveOsClient archiveOsClient;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final boolean internalSyntheticPublishEnabled;
    private final int maxRetryCount;
    private final int batchSize;
    private final AtomicBoolean publishing = new AtomicBoolean(false);

    public MarketOutboxPublisher(MarketOutboxRepository outboxRepository, NexusClient nexusClient,
                                 LedgerClient ledgerClient, ArchiveOsClient archiveOsClient, Clock clock, ObjectMapper objectMapper,
                                 @Value("${market.integration.internal-synthetic-publish-enabled:false}") boolean internalSyntheticPublishEnabled,
                                 @Value("${market.outbox.max-retry-count:5}") int maxRetryCount,
                                 @Value("${market.outbox.scheduler.batch-size:10}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.nexusClient = nexusClient;
        this.ledgerClient = ledgerClient;
        this.archiveOsClient = archiveOsClient;
        this.clock = clock;
        this.objectMapper = objectMapper;
        this.internalSyntheticPublishEnabled = internalSyntheticPublishEnabled;
        this.maxRetryCount = Math.max(1, maxRetryCount);
        this.batchSize = Math.max(1, Math.min(batchSize, 50));
    }

    @Transactional
    public List<MarketOutboxEntity> publishPending() {
        if (!publishing.compareAndSet(false, true)) {
            return List.of();
        }
        try {
            return publishApprovedEvents();
        } finally {
            publishing.set(false);
        }
    }

    /** Publishes exactly one identified event; it never selects a backlog candidate. */
    @Transactional
    public MarketOutboxEntity publishEvent(String eventId) {
        MarketOutboxEntity event = outboxRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + eventId));
        if (event.getStatus() == OutboxStatus.PUBLISHED || event.getStatus() == OutboxStatus.SKIPPED
                || event.getStatus() == OutboxStatus.DEAD_LETTER) return event;
        Instant now = Instant.now(clock);
        try {
            event.markPublishing();
            switch (event.getTargetService()) {
                case NEXUS -> nexusClient.publish(event.getPayload());
                case LOGISTICS -> throw new IllegalStateException("Market does not directly publish Logistics events; Nexus owns shipment relay");
                case LEDGER -> ledgerClient.publish(event.getPayload());
                case ARCHIVE_OS -> archiveOsClient.publish(event.getPayload());
            }
            event.markPublished(now);
        } catch (ArchiveOsPublishException ex) {
            if (ex.isRetryable()) retryOrDeadLetter(event, now, ex);
            else event.markDeadLetter(now, safeMessage(ex));
        } catch (RuntimeException ex) {
            retryOrDeadLetter(event, now, ex);
        }
        return event;
    }

    private List<MarketOutboxEntity> publishApprovedEvents() {
        Instant now = Instant.now(clock);
        List<MarketOutboxEntity> priority = outboxRepository.findNewestPublishable(
                OutboxStatus.PENDING,
                List.of(OutboxStatus.RETRY, OutboxStatus.RETRY_WAIT),
                now,
                PageRequest.of(0, Math.max(batchSize * 10, 50))).stream()
                .filter(this::isFreshApprovedSyntheticEvent)
                .limit(Math.min(batchSize, 10))
                .toList();
        // Never mutate legacy events merely to make room: FIFO is used only when no fresh eligible event exists.
        List<MarketOutboxEntity> events = priority.isEmpty() ? outboxRepository.findPublishable(
                OutboxStatus.PENDING, List.of(OutboxStatus.RETRY, OutboxStatus.RETRY_WAIT), now,
                PageRequest.of(0, Math.min(batchSize, 2))) : priority;
        for (MarketOutboxEntity event : events) {
            if (!internalSyntheticPublishEnabled) {
                event.markDryRun(now);
                continue;
            }
            try {
                event.markPublishing();
                switch (event.getTargetService()) {
                    case NEXUS -> nexusClient.publish(event.getPayload());
                    case LOGISTICS -> throw new IllegalStateException(
                            "Market does not directly publish Logistics events; Nexus owns shipment relay");
                    case LEDGER -> ledgerClient.publish(event.getPayload());
                    case ARCHIVE_OS -> archiveOsClient.publish(event.getPayload());
                }
                event.markPublished(now);
            } catch (ArchiveOsPublishException ex) {
                if (ex.isRetryable()) {
                    retryOrDeadLetter(event, now, ex);
                } else {
                    event.markDeadLetter(now, safeMessage(ex));
                }
            } catch (RuntimeException ex) {
                retryOrDeadLetter(event, now, ex);
            }
        }
        return events;
    }

    private boolean isFreshApprovedSyntheticEvent(MarketOutboxEntity event) {
        if (!event.isPublishApproved() || event.getRetryCount() >= maxRetryCount || event.getTargetService() == null) return false;
        try {
            var payload = objectMapper.readValue(event.getPayload(), new TypeReference<java.util.Map<String, Object>>() { });
            return Boolean.TRUE.equals(payload.get("syntheticData"))
                    && text(payload.get("correlationId")) != null
                    && text(payload.get("orderId")) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String text(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isBlank() ? null : text;
    }

    private void retryOrDeadLetter(MarketOutboxEntity event, Instant now, RuntimeException ex) {
                if (event.getRetryCount() + 1 >= maxRetryCount) {
            event.markDeadLetter(now, "Retry limit reached: " + safeMessage(ex));
                } else {
                    event.markFailed(safeMessage(ex), now.plusSeconds(backoffSeconds(event.getRetryCount())));
                }
    }

    public boolean isInternalSyntheticPublishEnabled() {
        return internalSyntheticPublishEnabled;
    }

    private long backoffSeconds(int retryCount) {
        return Math.min(300, 30L * (1L << Math.min(retryCount, 3)));
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message.substring(0, Math.min(300, message.length()));
    }
}
