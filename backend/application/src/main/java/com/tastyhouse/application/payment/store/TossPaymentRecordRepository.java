package com.tastyhouse.application.payment.store;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;

public interface TossPaymentRecordRepository {
    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
