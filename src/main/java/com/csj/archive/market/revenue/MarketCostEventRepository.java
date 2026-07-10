package com.csj.archive.market.revenue;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarketCostEventRepository extends JpaRepository<MarketCostEventEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("select coalesce(sum(e.costAmount), 0) from MarketCostEventEntity e")
    BigDecimal totalCost();
}
