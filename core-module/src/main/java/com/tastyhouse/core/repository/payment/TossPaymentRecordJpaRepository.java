package com.tastyhouse.core.repository.payment;

import com.tastyhouse.core.entity.payment.TossPaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TossPaymentRecordJpaRepository extends JpaRepository<TossPaymentRecord, Long> {
}
