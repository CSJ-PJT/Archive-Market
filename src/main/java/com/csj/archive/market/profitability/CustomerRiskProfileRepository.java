package com.csj.archive.market.profitability;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRiskProfileRepository extends JpaRepository<CustomerRiskProfileEntity, Long> {
    Optional<CustomerRiskProfileEntity> findByCustomerId(String customerId);
}
