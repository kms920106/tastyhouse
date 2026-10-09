package com.tastyhouse.infrastructure.jpa.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordSavePort;

@Repository
class TossPaymentRecordPersistenceAdapter implements TossPaymentRecordSavePort {

    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;

    public TossPaymentRecordPersistenceAdapter(TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository) {
        this.tossPaymentRecordJpaRepository = tossPaymentRecordJpaRepository;
    }

    @Override
    public TossPaymentRecord save(TossPaymentRecord tossPaymentRecord) {
        TossPaymentRecordJpaEntity saved = tossPaymentRecordJpaRepository.save(TossPaymentRecordMapper.toEntity(tossPaymentRecord));
        return TossPaymentRecordMapper.toDomain(saved);
    }
}
