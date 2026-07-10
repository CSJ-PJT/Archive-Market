package com.csj.archive.market.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface MarketOutboxRepository extends JpaRepository<MarketOutboxEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    List<MarketOutboxEntity> findTop100ByStatusInOrderByCreatedAtAsc(List<OutboxStatus> statuses);

    List<MarketOutboxEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(OutboxStatus status);
}
