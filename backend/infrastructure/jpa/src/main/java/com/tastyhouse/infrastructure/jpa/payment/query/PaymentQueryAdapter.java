package com.tastyhouse.infrastructure.jpa.payment.query;

import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentRefundResult;
import com.tastyhouse.application.payment.port.out.PaymentResult;

import static com.tastyhouse.infrastructure.jpa.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.jpa.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;
import static com.tastyhouse.infrastructure.jpa.payment.persistence.QPaymentRefundJpaEntity.paymentRefundJpaEntity;

@Repository
class PaymentQueryAdapter implements PaymentQueryPort {

    private final JPAQueryFactory queryFactory;

    public PaymentQueryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<PaymentResult> findPaymentByOrderId(Long orderId) {
        return Optional.ofNullable(
            selectPayment()
                .where(paymentJpaEntity.orderId.eq(orderId))
                .fetchOne()
        );
    }

    @Override
    public Optional<PaymentResult> findPaymentById(Long paymentId) {
        return Optional.ofNullable(
            selectPayment()
                .where(paymentJpaEntity.id.eq(paymentId))
                .fetchOne()
        );
    }

    @Override
    public Optional<PaymentRefundResult> findRefundById(Long refundId) {
        PaymentRefundResult result = queryFactory
            .select(Projections.constructor(PaymentRefundResult.class,
                paymentRefundJpaEntity.id,
                paymentRefundJpaEntity.paymentId,
                paymentRefundJpaEntity.refundAmount,
                paymentRefundJpaEntity.refundReason,
                paymentRefundJpaEntity.refundStatus,
                paymentRefundJpaEntity.pgRefundId,
                paymentRefundJpaEntity.refundedAt,
                paymentRefundJpaEntity.createdAt
            ))
            .from(paymentRefundJpaEntity)
            .where(paymentRefundJpaEntity.id.eq(refundId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    private JPAQuery<PaymentResult> selectPayment() {
        return queryFactory
            .select(Projections.constructor(PaymentResult.class,
                paymentJpaEntity.id,
                paymentJpaEntity.orderId,
                orderJpaEntity.memberId,
                paymentJpaEntity.paymentMethod,
                paymentJpaEntity.paymentStatus,
                paymentJpaEntity.amount,
                paymentJpaEntity.pgProvider,
                paymentJpaEntity.pgTid,
                paymentJpaEntity.pgOrderId,
                paymentJpaEntity.cardCompany,
                paymentJpaEntity.cardNumber,
                paymentJpaEntity.installmentMonths,
                paymentJpaEntity.approvedAt,
                paymentJpaEntity.cancelledAt,
                paymentJpaEntity.cancelReason,
                paymentJpaEntity.receiptUrl,
                paymentJpaEntity.createdAt
            ))
            .from(paymentJpaEntity)
            .innerJoin(orderJpaEntity).on(orderJpaEntity.id.eq(paymentJpaEntity.orderId));
    }
}
