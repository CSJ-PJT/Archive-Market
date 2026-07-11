package com.csj.archive.market.revenue;

import java.math.BigDecimal;
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
}
