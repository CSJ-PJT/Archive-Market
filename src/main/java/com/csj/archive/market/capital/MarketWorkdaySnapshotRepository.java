package com.csj.archive.market.capital;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketWorkdaySnapshotRepository extends JpaRepository<MarketWorkdaySnapshotEntity, Long> {
    Optional<MarketWorkdaySnapshotEntity> findTopByOrderByWorkDateDescCreatedAtDesc();

    List<MarketWorkdaySnapshotEntity> findByOrderByCreatedAtDesc(Pageable pageable);
}
