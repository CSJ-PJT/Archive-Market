package com.csj.archive.market.outbox;

import com.csj.archive.market.integration.archiveos.ArchiveOsClient;
import com.csj.archive.market.integration.ledger.LedgerClient;
import com.csj.archive.market.integration.nexus.NexusClient;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketOutboxPublisher {

    private final MarketOutboxRepository outboxRepository;
    private final NexusClient nexusClient;
    private final LedgerClient ledgerClient;
    private final ArchiveOsClient archiveOsClient;
    private final Clock clock;
    private final boolean integrationEnabled;
    private final int maxRetryCount;

    public MarketOutboxPublisher(MarketOutboxRepository outboxRepository, NexusClient nexusClient,
                                 LedgerClient ledgerClient, ArchiveOsClient archiveOsClient, Clock clock,
                                 @Value("${market.integration.enabled:false}") boolean integrationEnabled,
                                 @Value("${market.outbox.max-retry-count:5}") int maxRetryCount) {
        this.outboxRepository = outboxRepository;
        this.nexusClient = nexusClient;
        this.ledgerClient = ledgerClient;
        this.archiveOsClient = archiveOsClient;
        this.clock = clock;
        this.integrationEnabled = integrationEnabled;
        this.maxRetryCount = Math.max(1, maxRetryCount);
    }

    @Transactional
    public List<MarketOutboxEntity> publishPending() {
        List<MarketOutboxEntity> events = outboxRepository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.RETRY));
        Instant now = Instant.now(clock);
        for (MarketOutboxEntity event : events) {
            if (!integrationEnabled) {
                event.markDryRun(now);
                continue;
            }
            try {
                switch (event.getTargetService()) {
                    case NEXUS -> nexusClient.publish(event.getPayload());
                    case LOGISTICS -> event.markDryRun(now);
                    case LEDGER -> ledgerClient.publish(event.getPayload());
                    case ARCHIVE_OS -> archiveOsClient.publish(event.getPayload());
                }
                if (event.getStatus() != OutboxStatus.DRY_RUN) {
                    event.markPublished(now);
                }
            } catch (RuntimeException ex) {
                if (event.getRetryCount() + 1 >= maxRetryCount) {
                    event.markSkipped(now, "Retry limit reached: " + safeMessage(ex));
                } else {
                    event.markFailed(safeMessage(ex), now.plusSeconds(backoffSeconds(event.getRetryCount())));
                }
            }
        }
        return events;
    }

    private long backoffSeconds(int retryCount) {
        return Math.min(300, 30L * (1L << Math.min(retryCount, 3)));
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message.substring(0, Math.min(300, message.length()));
    }
}
