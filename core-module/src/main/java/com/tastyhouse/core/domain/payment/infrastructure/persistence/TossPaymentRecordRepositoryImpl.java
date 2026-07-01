package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.core.domain.payment.domain.repository.TossPaymentRecordRepository;

@Repository
@RequiredArgsConstructor
public class TossPaymentRecordRepositoryImpl implements TossPaymentRecordRepository {

    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;

    @Override
    public TossPaymentRecord save(TossPaymentRecord tossPaymentRecord) {
        return tossPaymentRecordJpaRepository.save(tossPaymentRecord);
    }
}
