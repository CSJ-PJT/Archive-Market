package com.csj.archive.market.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarketOutboxPublishScheduler {

    private final MarketOutboxPublisher publisher;
    private final boolean enabled;

    public MarketOutboxPublishScheduler(MarketOutboxPublisher publisher,
                                        @Value("${market.outbox.scheduler.enabled:false}") boolean enabled) {
        this.publisher = publisher;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${market.outbox.scheduler.fixed-delay-ms:10000}")
    public void publishApprovedSyntheticEvents() {
        if (enabled) {
            publisher.publishPending();
        }
    }
}
