package com.tastyhouse.core.repository.payment;

import com.tastyhouse.core.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
