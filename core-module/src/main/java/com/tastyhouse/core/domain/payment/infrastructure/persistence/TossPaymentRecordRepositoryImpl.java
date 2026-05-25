package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.core.domain.payment.domain.repository.TossPaymentRecordRepository;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.domain.payment.domain.model.QTossPaymentRecord.tossPaymentRecord;

@Repository
@RequiredArgsConstructor
public class TossPaymentRecordRepositoryImpl implements TossPaymentRecordRepository {

    private final JPAQueryFactory queryFactory;
    private final TossPaymentRecordJpaRepository tossPaymentRecordJpaRepository;

    @Override
    public Optional<TossPaymentRecord> findByPaymentId(PaymentId paymentId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(tossPaymentRecord)
                .where(tossPaymentRecord.paymentId.eq(paymentId.value()))
                .fetchOne()
        );
    }

    @Override
    public Optional<TossPaymentRecord> findByPaymentKey(String paymentKey) {
        return Optional.ofNullable(
            queryFactory.selectFrom(tossPaymentRecord)
                .where(tossPaymentRecord.paymentKey.eq(paymentKey))
                .fetchOne()
        );
    }

    @Override
    public Optional<TossPaymentRecord> findByOrderId(String orderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(tossPaymentRecord)
                .where(tossPaymentRecord.orderId.eq(orderId))
                .fetchOne()
        );
    }

    @Override
    public TossPaymentRecord save(TossPaymentRecord tossPaymentRecord) {
        return tossPaymentRecordJpaRepository.save(tossPaymentRecord);
    }
}
