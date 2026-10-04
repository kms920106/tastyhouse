package com.tastyhouse.infrastructure.persistence.payment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TossPaymentRecordJpaRepository extends JpaRepository<TossPaymentRecordJpaEntity, Long> {
}
