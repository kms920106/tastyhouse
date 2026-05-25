package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import com.tastyhouse.core.domain.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
