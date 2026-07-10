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

    public MarketOutboxPublisher(MarketOutboxRepository outboxRepository, NexusClient nexusClient,
                                 LedgerClient ledgerClient, ArchiveOsClient archiveOsClient, Clock clock,
                                 @Value("${market.integration.enabled:false}") boolean integrationEnabled) {
        this.outboxRepository = outboxRepository;
        this.nexusClient = nexusClient;
        this.ledgerClient = ledgerClient;
        this.archiveOsClient = archiveOsClient;
        this.clock = clock;
        this.integrationEnabled = integrationEnabled;
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
                    case LEDGER -> ledgerClient.publish(event.getPayload());
                    case ARCHIVE_OS -> archiveOsClient.publish(event.getPayload());
                }
                event.markPublished(now);
            } catch (RuntimeException ex) {
                event.markFailed(ex.getMessage(), now.plusSeconds(60));
            }
        }
        return events;
    }
}
