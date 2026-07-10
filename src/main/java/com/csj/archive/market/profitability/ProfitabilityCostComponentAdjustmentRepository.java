package com.csj.archive.market.profitability;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfitabilityCostComponentAdjustmentRepository
        extends JpaRepository<ProfitabilityCostComponentAdjustmentEntity, Long> {

    boolean existsBySourceEventIdOrIdempotencyKey(String sourceEventId, String idempotencyKey);

    List<ProfitabilityCostComponentAdjustmentEntity> findByOrderIdOrderByCreatedAtDesc(String orderId);
}
