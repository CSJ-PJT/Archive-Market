package com.csj.archive.market.profitability;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderProfitabilityAssessmentRepository extends JpaRepository<OrderProfitabilityAssessmentEntity, Long> {
    Optional<OrderProfitabilityAssessmentEntity> findTopByOrderIdOrderByCreatedAtDesc(String orderId);

    List<OrderProfitabilityAssessmentEntity> findByOrderByCreatedAtDesc(Pageable pageable);

    long countByRecommendation(ProfitabilityRecommendation recommendation);

    long countByMarginRateLessThan(BigDecimal marginRate);

    long countByRiskScoreGreaterThanEqual(BigDecimal riskScore);

    @Query(value = "select coalesce(avg(margin_rate), 0) from order_profitability_assessment", nativeQuery = true)
    BigDecimal averageMarginRate();

    @Query(value = "select coalesce(sum(expected_profit), 0) from order_profitability_assessment", nativeQuery = true)
    BigDecimal totalExpectedProfit();
}
