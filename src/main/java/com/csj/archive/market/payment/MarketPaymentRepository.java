package com.csj.archive.market.payment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPaymentRepository extends JpaRepository<MarketPaymentEntity, Long> {
    Optional<MarketPaymentEntity> findByOrderId(String orderId);

    long countByPaymentStatus(PaymentStatus paymentStatus);
}
