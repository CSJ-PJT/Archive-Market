package com.csj.archive.market.revenue;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarketRevenueEventRepository extends JpaRepository<MarketRevenueEventEntity, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("select coalesce(sum(e.revenueAmount), 0) from MarketRevenueEventEntity e")
    BigDecimal totalRevenue();
}
