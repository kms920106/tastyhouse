package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;

public interface TossPaymentRecordRepository {

    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
