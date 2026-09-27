package com.tastyhouse.application.payment.store;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordStatePort;

public class TossPaymentRecordStore implements TossPaymentRecordRepository {
    private final TossPaymentRecordStatePort tossPaymentRecordStatePort;

    public TossPaymentRecordStore(TossPaymentRecordStatePort tossPaymentRecordStatePort) {
        this.tossPaymentRecordStatePort = tossPaymentRecordStatePort;
    }

    @Override
    public TossPaymentRecord save(TossPaymentRecord tossPaymentRecord) {
        return TossPaymentRecordStateMapper.toDomain(
            tossPaymentRecordStatePort.save(TossPaymentRecordStateMapper.toState(tossPaymentRecord))
        );
    }
}
