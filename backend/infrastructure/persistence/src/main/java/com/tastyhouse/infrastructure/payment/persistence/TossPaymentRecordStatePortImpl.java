package com.tastyhouse.infrastructure.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordState;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordStatePort;

@Repository
public class TossPaymentRecordStatePortImpl implements TossPaymentRecordStatePort {
    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;

    public TossPaymentRecordStatePortImpl(TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository) {
        this.tossPaymentRecordJpaRepository = tossPaymentRecordJpaRepository;
    }

    @Override
    public TossPaymentRecordState save(TossPaymentRecordState state) {
        TossPaymentRecordJpaEntity saved = tossPaymentRecordJpaRepository.save(TossPaymentRecordMapper.toEntity(state));
        return TossPaymentRecordMapper.toState(saved);
    }
}
