package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TossPaymentRecordJpaRepository extends JpaRepository<TossPaymentRecord, Long> {
}
