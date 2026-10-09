package com.tastyhouse.infrastructure.jpa.payment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {
}
