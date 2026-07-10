package com.csj.archive.market.revenue;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketDailyCloseRepository extends JpaRepository<MarketDailyCloseEntity, Long> {
    Optional<MarketDailyCloseEntity> findTopByOrderByCloseDateDescCreatedAtDesc();
}
