package com.tastyhouse.infrastructure.payment.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.vo.PaymentId;

import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    private final JPAQueryFactory queryFactory;
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryImpl(JPAQueryFactory queryFactory, PaymentJpaRepository paymentJpaRepository) {
        this.queryFactory = queryFactory;
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentJpaRepository.findById(paymentId.value()).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(paymentJpaEntity)
                .where(paymentJpaEntity.pgOrderId.eq(pgOrderId))
                .fetchOne()
        ).map(PaymentMapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return queryFactory.selectOne().from(paymentJpaEntity)
            .where(paymentJpaEntity.orderId.eq(orderId.value()))
            .fetchFirst() != null;
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            PaymentJpaEntity saved = paymentJpaRepository.save(PaymentMapper.toEntity(payment));
            return PaymentMapper.toDomain(saved);
        }

        PaymentJpaEntity entity = paymentJpaRepository.findById(payment.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 결제입니다: " + payment.getId()));
        PaymentMapper.applyChanges(entity, payment);
        return PaymentMapper.toDomain(entity);
    }
}
