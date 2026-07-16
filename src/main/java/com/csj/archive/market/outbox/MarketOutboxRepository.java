package com.csj.archive.market.outbox;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketOutboxRepository extends JpaRepository<MarketOutboxEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<MarketOutboxEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<MarketOutboxEntity> findByEventId(String eventId);

    List<MarketOutboxEntity> findTop100ByStatusInOrderByCreatedAtAsc(List<OutboxStatus> statuses);

    @Query("""
            select e from MarketOutboxEntity e
            where e.publishApproved = true
              and (e.status = :pending
                   or (e.status in :retryStatuses and (e.nextRetryAt is null or e.nextRetryAt <= :now)))
            order by e.createdAt asc
            """)
    List<MarketOutboxEntity> findPublishable(@Param("pending") OutboxStatus pending,
                                             @Param("retryStatuses") List<OutboxStatus> retryStatuses,
                                             @Param("now") Instant now,
                                             Pageable pageable);

    @Query("""
            select e from MarketOutboxEntity e
            where e.publishApproved = true
              and (e.status = :pending
                   or (e.status in :retryStatuses and (e.nextRetryAt is null or e.nextRetryAt <= :now)))
            order by e.createdAt desc
            """)
    List<MarketOutboxEntity> findNewestPublishable(@Param("pending") OutboxStatus pending,
                                                   @Param("retryStatuses") List<OutboxStatus> retryStatuses,
                                                   @Param("now") Instant now,
                                                   Pageable pageable);

    List<MarketOutboxEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    List<MarketOutboxEntity> findByAggregateIdOrderByCreatedAtAsc(String aggregateId);

    long countByStatus(OutboxStatus status);
}
