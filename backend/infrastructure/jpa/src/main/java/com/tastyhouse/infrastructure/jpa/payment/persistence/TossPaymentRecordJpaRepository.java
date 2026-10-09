package com.tastyhouse.infrastructure.jpa.payment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface TossPaymentRecordJpaRepository extends JpaRepository<TossPaymentRecordJpaEntity, Long> {
}
