package com.csj.archive.market.revenue;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarketCostEventRepository extends JpaRepository<MarketCostEventEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("select coalesce(sum(e.costAmount), 0) from MarketCostEventEntity e")
    BigDecimal totalCost();

    @Query("select coalesce(sum(e.costAmount), 0) from MarketCostEventEntity e where e.costType in :types")
    BigDecimal totalCostByTypes(Iterable<CostType> types);

    @Query("select min(e.createdAt) from MarketCostEventEntity e")
    java.util.Optional<Instant> findEarliestCreatedAt();

    @Query("select max(e.createdAt) from MarketCostEventEntity e")
    java.util.Optional<Instant> findLatestCreatedAt();
}
