package com.csj.archive.market.payment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarketPaymentRepository extends JpaRepository<MarketPaymentEntity, Long> {
    Optional<MarketPaymentEntity> findByOrderId(String orderId);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    @Query("select coalesce(sum(p.amount), 0) from MarketPaymentEntity p where p.paymentStatus = :paymentStatus")
    java.math.BigDecimal totalAmountByPaymentStatus(PaymentStatus paymentStatus);
}
