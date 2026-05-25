package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

import java.util.Optional;

public interface TossPaymentRecordRepository {

    Optional<TossPaymentRecord> findByPaymentId(PaymentId paymentId);

    Optional<TossPaymentRecord> findByPaymentKey(String paymentKey);

    Optional<TossPaymentRecord> findByOrderId(String orderId);

    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
