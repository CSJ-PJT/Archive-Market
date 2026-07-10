package com.csj.archive.market.profitability;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingPolicyRepository extends JpaRepository<PricingPolicyEntity, Long> {
    boolean existsByPolicyCode(String policyCode);

    List<PricingPolicyEntity> findByEnabledTrueOrderByPolicyCodeAsc();
}
