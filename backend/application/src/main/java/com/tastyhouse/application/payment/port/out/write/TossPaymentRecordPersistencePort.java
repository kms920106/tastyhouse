package com.tastyhouse.application.payment.port.out.write;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;

public interface TossPaymentRecordPersistencePort {

    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
