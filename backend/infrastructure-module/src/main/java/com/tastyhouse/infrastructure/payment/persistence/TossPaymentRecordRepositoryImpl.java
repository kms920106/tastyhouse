package com.tastyhouse.infrastructure.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.repository.TossPaymentRecordRepository;

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
