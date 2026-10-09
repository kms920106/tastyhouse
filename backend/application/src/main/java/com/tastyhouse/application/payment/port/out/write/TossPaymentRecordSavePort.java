package com.tastyhouse.application.payment.port.out.write;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;

public interface TossPaymentRecordSavePort {

    TossPaymentRecord save(TossPaymentRecord tossPaymentRecord);
}
