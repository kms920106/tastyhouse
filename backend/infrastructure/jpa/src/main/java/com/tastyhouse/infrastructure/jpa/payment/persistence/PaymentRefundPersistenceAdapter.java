package com.tastyhouse.infrastructure.jpa.payment.persistence;

import java.util.Set;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundSavePort;

import static com.tastyhouse.infrastructure.jpa.payment.persistence.QPaymentRefundJpaEntity.paymentRefundJpaEntity;

@Repository
class PaymentRefundPersistenceAdapter implements PaymentRefundLoadPort, PaymentRefundSavePort {

    private final JPAQueryFactory queryFactory;
    private final PaymentRefundJpaRepository paymentRefundJpaRepository;

    public PaymentRefundPersistenceAdapter(
        JPAQueryFactory queryFactory,
        PaymentRefundJpaRepository paymentRefundJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.paymentRefundJpaRepository = paymentRefundJpaRepository;
    }

    @Override
    public long findRefundAmountSum(PaymentId paymentId, Set<RefundStatus> statuses) {
        if (statuses.isEmpty()) {
            return 0L;
        }
        Long sum = queryFactory
            .select(paymentRefundJpaEntity.refundAmount.sumLong().coalesce(0L))
            .from(paymentRefundJpaEntity)
            .where(
                paymentRefundJpaEntity.paymentId.eq(paymentId.value()),
                paymentRefundJpaEntity.refundStatus.in(statuses.stream().map(RefundStatus::name).toList())
            )
            .fetchOne();
        return sum == null ? 0L : sum;
    }

    @Override
    public PaymentRefund save(PaymentRefund paymentRefund) {
        PaymentRefundJpaEntity saved = paymentRefundJpaRepository.save(PaymentRefundMapper.toEntity(paymentRefund));
        return PaymentRefundMapper.toDomain(saved);
    }
}
