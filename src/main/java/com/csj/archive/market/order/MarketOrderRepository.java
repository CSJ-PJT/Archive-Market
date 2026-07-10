package com.csj.archive.market.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketOrderRepository extends JpaRepository<MarketOrderEntity, Long> {
    Optional<MarketOrderEntity> findByOrderId(String orderId);

    long countByOrderStatus(OrderStatus orderStatus);

    long countByRiskScoreGreaterThanEqual(int riskScore);
}
