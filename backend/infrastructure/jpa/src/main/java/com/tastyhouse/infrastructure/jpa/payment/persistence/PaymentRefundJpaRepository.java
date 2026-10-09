package com.tastyhouse.infrastructure.jpa.payment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentRefundJpaRepository extends JpaRepository<PaymentRefundJpaEntity, Long> {
}
