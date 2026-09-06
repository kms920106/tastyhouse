package com.tastyhouse.domain.payment.repository;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;

public interface TossPaymentRecordRepository {
    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
