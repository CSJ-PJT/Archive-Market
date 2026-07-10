package com.csj.archive.market.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketOutboxRepository extends JpaRepository<MarketOutboxEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    List<MarketOutboxEntity> findTop100ByStatusInOrderByCreatedAtAsc(List<OutboxStatus> statuses);

    long countByStatus(OutboxStatus status);
}
