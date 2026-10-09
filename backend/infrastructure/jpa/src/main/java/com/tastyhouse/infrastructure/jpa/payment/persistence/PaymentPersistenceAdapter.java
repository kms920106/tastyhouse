package com.tastyhouse.infrastructure.jpa.payment.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;

import static com.tastyhouse.infrastructure.jpa.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;

@Repository
class PaymentPersistenceAdapter implements PaymentLoadPort, PaymentSavePort {

    private final JPAQueryFactory queryFactory;
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentPersistenceAdapter(JPAQueryFactory queryFactory, PaymentJpaRepository paymentJpaRepository) {
        this.queryFactory = queryFactory;
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentJpaRepository.findById(paymentId.value()).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        PaymentJpaEntity entity = queryFactory
            .selectFrom(paymentJpaEntity)
            .where(paymentJpaEntity.pgOrderId.eq(pgOrderId))
            .fetchOne();
        return Optional.ofNullable(entity).map(PaymentMapper::toDomain);
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
