package com.csj.archive.market.revenue;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketProfitSnapshotRepository extends JpaRepository<MarketProfitSnapshotEntity, Long> {
    Optional<MarketProfitSnapshotEntity> findTopByOrderBySnapshotDateDescCreatedAtDesc();
}
