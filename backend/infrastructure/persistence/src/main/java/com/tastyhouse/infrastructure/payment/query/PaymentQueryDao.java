package com.tastyhouse.infrastructure.payment.query;

import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentRefundResult;
import com.tastyhouse.application.payment.port.out.PaymentResult;
import com.querydsl.core.types.Projections;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;

import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;
import static com.tastyhouse.infrastructure.payment.persistence.QPaymentRefundJpaEntity.paymentRefundJpaEntity;

@Repository
public class PaymentQueryDao implements PaymentQueryPort {
    private final JPAQueryFactory queryFactory;

    public PaymentQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<PaymentResult> findPaymentByOrderId(OrderId orderId) {
        return Optional.ofNullable(
            selectPayment()
                .where(paymentJpaEntity.orderId.eq(orderId.value()))
                .fetchOne()
        );
    }

    @Override
    public Optional<PaymentResult> findPaymentById(PaymentId paymentId) {
        return Optional.ofNullable(
            selectPayment()
                .where(paymentJpaEntity.id.eq(paymentId.value()))
                .fetchOne()
        );
    }

    @Override
    public Optional<PaymentRefundResult> findRefundById(PaymentRefundId refundId) {
        return Optional.ofNullable(
            queryFactory
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
                .where(paymentRefundJpaEntity.id.eq(refundId.value()))
                .fetchOne()
        );
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
