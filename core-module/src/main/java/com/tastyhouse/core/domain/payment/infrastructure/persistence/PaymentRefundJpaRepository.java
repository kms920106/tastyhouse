package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundJpaRepository extends JpaRepository<PaymentRefund, Long> {
}
