package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;

public interface TossPaymentRecordJpaRepository extends JpaRepository<TossPaymentRecord, Long> {
}
