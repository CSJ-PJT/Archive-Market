package com.csj.archive.market.claim;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketReturnRepository extends JpaRepository<MarketReturnEntity, Long> {
    boolean existsByOrderId(String orderId);
}
