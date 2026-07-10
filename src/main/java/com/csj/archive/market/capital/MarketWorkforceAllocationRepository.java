package com.csj.archive.market.capital;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketWorkforceAllocationRepository extends JpaRepository<MarketWorkforceAllocationEntity, Long> {
    Optional<MarketWorkforceAllocationEntity> findByWorkdayIdAndWorkforceRole(String workdayId, WorkforceRole workforceRole);

    List<MarketWorkforceAllocationEntity> findByWorkdayIdAndEnabledTrueOrderByWorkforceRoleAsc(String workdayId);

    long countByWorkdayId(String workdayId);
}
