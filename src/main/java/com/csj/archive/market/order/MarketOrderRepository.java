package com.csj.archive.market.order;

import java.util.Optional;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarketOrderRepository extends JpaRepository<MarketOrderEntity, Long> {
    Optional<MarketOrderEntity> findByOrderId(String orderId);

    long countByOrderStatus(OrderStatus orderStatus);

    long countByOrderStatusIn(Iterable<OrderStatus> orderStatuses);

    long countByRiskScoreGreaterThanEqual(int riskScore);

    @Query("select coalesce(sum(o.totalOrderAmount), 0) from MarketOrderEntity o")
    java.math.BigDecimal totalGmv();

    @Query("select coalesce(sum(o.paymentAmount), 0) from MarketOrderEntity o")
    java.math.BigDecimal totalPaymentAmount();

    @Query("select min(o.createdAt) from MarketOrderEntity o")
    Optional<Instant> findEarliestCreatedAt();

    @Query("select max(o.createdAt) from MarketOrderEntity o")
    Optional<Instant> findLatestCreatedAt();
}
