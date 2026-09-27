package com.tastyhouse.infrastructure.payment.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.payment.port.out.write.PaymentState;
import com.tastyhouse.application.payment.port.out.write.PaymentStatePort;

import static com.tastyhouse.infrastructure.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;

@Repository
public class PaymentStatePortImpl implements PaymentStatePort {
    private final JPAQueryFactory queryFactory;
    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentStatePortImpl(JPAQueryFactory queryFactory, PaymentJpaRepository paymentJpaRepository) {
        this.queryFactory = queryFactory;
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Optional<PaymentState> findById(Long paymentId) {
        return paymentJpaRepository.findById(paymentId).map(PaymentMapper::toState);
    }

    @Override
    public Optional<PaymentState> findByPgOrderId(String pgOrderId) {
        return Optional.ofNullable(
            queryFactory.selectFrom(paymentJpaEntity)
                .where(paymentJpaEntity.pgOrderId.eq(pgOrderId))
                .fetchOne()
        ).map(PaymentMapper::toState);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return queryFactory.selectOne().from(paymentJpaEntity)
            .where(paymentJpaEntity.orderId.eq(orderId))
            .fetchFirst() != null;
    }

    @Override
    public PaymentState save(PaymentState state) {
        if (state.id() == null) {
            PaymentJpaEntity saved = paymentJpaRepository.save(PaymentMapper.toEntity(state));
            return PaymentMapper.toState(saved);
        }

        PaymentJpaEntity entity = paymentJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 결제입니다: " + state.id()));
        PaymentMapper.applyChanges(entity, state);
        return PaymentMapper.toState(entity);
    }
}
