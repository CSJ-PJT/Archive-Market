package com.csj.archive.market.inbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketInboxRepository extends JpaRepository<MarketInboxEntity, Long> {
    boolean existsByEventIdOrIdempotencyKey(String eventId, String idempotencyKey);

    List<MarketInboxEntity> findByOrderByReceivedAtDesc(Pageable pageable);

    long countByStatus(MarketInboxStatus status);
}
