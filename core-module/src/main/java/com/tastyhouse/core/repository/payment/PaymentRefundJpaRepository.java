package com.tastyhouse.core.repository.payment;

import com.tastyhouse.core.entity.payment.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRefundJpaRepository extends JpaRepository<PaymentRefund, Long> {
}
