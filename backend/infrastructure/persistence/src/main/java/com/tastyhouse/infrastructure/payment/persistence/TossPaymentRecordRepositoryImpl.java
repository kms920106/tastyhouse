package com.tastyhouse.infrastructure.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordRepository;
import com.tastyhouse.domain.payment.model.TossPaymentRecord;

@Repository
public class TossPaymentRecordRepositoryImpl implements TossPaymentRecordRepository {
    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;

    public TossPaymentRecordRepositoryImpl(TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository) {
        this.tossPaymentRecordJpaRepository = tossPaymentRecordJpaRepository;
    }

    @Override
    public TossPaymentRecord save(TossPaymentRecord tossPaymentRecord) {
        TossPaymentRecordJpaEntity saved = tossPaymentRecordJpaRepository.save(TossPaymentRecordMapper.toEntity(tossPaymentRecord));
        return TossPaymentRecordMapper.toDomain(saved);
    }
}
