package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.core.domain.payment.domain.model.RefundStatus;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRefundRepository;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.payment.domain.model.QPaymentRefund.paymentRefund;

@Repository
@RequiredArgsConstructor
public class PaymentRefundRepositoryImpl implements PaymentRefundRepository {

    private final JPAQueryFactory queryFactory;
    private final PaymentRefundJpaRepository paymentRefundJpaRepository;

    @Override
    public List<PaymentRefund> findByPaymentIdOrderByCreatedAtDesc(PaymentId paymentId) {
        return queryFactory.selectFrom(paymentRefund)
            .where(paymentRefund.paymentId.eq(paymentId))
            .orderBy(paymentRefund.createdAt.desc())
            .fetch();
    }

    @Override
    public Optional<PaymentRefund> findByPgRefundId(String pgRefundId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(paymentRefund)
                .where(paymentRefund.pgRefundId.eq(pgRefundId))
                .fetchOne()
        );
    }

    @Override
    public List<PaymentRefund> findByRefundStatusOrderByCreatedAtDesc(RefundStatus refundStatus) {
        return queryFactory.selectFrom(paymentRefund)
            .where(paymentRefund.refundStatus.eq(refundStatus))
            .orderBy(paymentRefund.createdAt.desc())
            .fetch();
    }

    @Override
    public PaymentRefund save(PaymentRefund paymentRefund) {
        return paymentRefundJpaRepository.save(paymentRefund);
    }
}
