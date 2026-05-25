package com.tastyhouse.core.domain.payment.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.payment.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.payment.domain.model.QPayment.payment;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JPAQueryFactory queryFactory;
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentJpaRepository.findById(paymentId.value());
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(payment)
                .where(payment.orderId.eq(orderId))
                .fetchOne()
        );
    }

    @Override
    public Optional<Payment> findByPgTid(String pgTid) {
        return Optional.ofNullable(
            queryFactory.selectFrom(payment)
                .where(payment.pgTid.eq(pgTid))
                .fetchOne()
        );
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(payment)
                .where(payment.pgOrderId.eq(pgOrderId))
                .fetchOne()
        );
    }

    @Override
    public List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus) {
        return queryFactory.selectFrom(payment)
            .where(payment.paymentStatus.eq(paymentStatus))
            .orderBy(payment.createdAt.desc())
            .fetch();
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return queryFactory.selectOne().from(payment)
            .where(payment.orderId.eq(orderId))
            .fetchFirst() != null;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }
}
