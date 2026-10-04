package com.tastyhouse.infrastructure.persistence.payment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentRefundJpaRepository extends JpaRepository<PaymentRefundJpaEntity, Long> {
}
