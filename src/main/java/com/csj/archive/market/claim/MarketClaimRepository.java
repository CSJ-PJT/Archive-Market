package com.csj.archive.market.claim;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketClaimRepository extends JpaRepository<MarketClaimEntity, Long> {
    boolean existsByOrderId(String orderId);
}
