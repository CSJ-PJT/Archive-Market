package com.csj.archive.market.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketInboxRepository extends JpaRepository<MarketInboxEntity, Long> {
    boolean existsByEventIdOrIdempotencyKey(String eventId, String idempotencyKey);

    long countByStatus(MarketInboxStatus status);
}
