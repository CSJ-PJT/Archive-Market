package com.csj.archive.market.revenue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarketRevenueEventRepository extends JpaRepository<MarketRevenueEventEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    List<MarketRevenueEventEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select coalesce(sum(e.revenueAmount), 0) from MarketRevenueEventEntity e")
    BigDecimal totalRevenue();

    @Query("select coalesce(sum(e.revenueAmount), 0) from MarketRevenueEventEntity e where e.revenueType in :types")
    BigDecimal totalRevenueByTypes(Iterable<RevenueType> types);

    @Query("select coalesce(sum(e.revenueAmount), 0) from MarketRevenueEventEntity e where e.revenueType in :types and e.createdAt >= :from and e.createdAt <= :to")
    BigDecimal totalRevenueByTypesBetween(Iterable<RevenueType> types, Instant from, Instant to);

    @Query("select max(e.createdAt) from MarketRevenueEventEntity e where e.createdAt >= :from and e.createdAt <= :to")
    java.util.Optional<Instant> findLatestCreatedAtBetween(Instant from, Instant to);

    @Query("select min(e.createdAt) from MarketRevenueEventEntity e")
    java.util.Optional<Instant> findEarliestCreatedAt();

    @Query("select max(e.createdAt) from MarketRevenueEventEntity e")
    java.util.Optional<Instant> findLatestCreatedAt();
}
