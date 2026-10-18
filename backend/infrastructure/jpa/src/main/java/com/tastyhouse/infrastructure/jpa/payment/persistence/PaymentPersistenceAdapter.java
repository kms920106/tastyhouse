package com.tastyhouse.infrastructure.jpa.payment.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentStatus;
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
    public Optional<Payment> findByIdForUpdate(PaymentId paymentId) {
        PaymentJpaEntity entity = queryFactory
            .selectFrom(paymentJpaEntity)
            .where(paymentJpaEntity.id.eq(paymentId.value()))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne();
        return Optional.ofNullable(entity).map(PaymentMapper::toDomain);
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
    public Optional<Payment> findByPgOrderIdForUpdate(String pgOrderId) {
        Long paymentId = queryFactory
            .select(paymentJpaEntity.id)
            .from(paymentJpaEntity)
            .where(paymentJpaEntity.pgOrderId.eq(pgOrderId))
            .fetchOne();
        if (paymentId == null) {
            return Optional.empty();
        }
        return findByIdForUpdate(PaymentId.of(paymentId));
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return queryFactory.selectOne().from(paymentJpaEntity)
            .where(paymentJpaEntity.orderId.eq(orderId.value()))
            .fetchFirst() != null;
    }

    @Override
    public List<Payment> findAllPgAttemptedReconcileDueByStatusCreatedBetween(
        PaymentStatus status, LocalDateTime from, LocalDateTime to, LocalDateTime dueAt, int limit
    ) {
        return queryFactory
            .selectFrom(paymentJpaEntity)
            .where(
                paymentJpaEntity.paymentStatus.eq(status.name()),
                paymentJpaEntity.pgProvider.isNotNull(),
                paymentJpaEntity.createdAt.goe(from),
                paymentJpaEntity.createdAt.lt(to),
                paymentJpaEntity.reconcileNextAt.isNull().or(paymentJpaEntity.reconcileNextAt.loe(dueAt))
            )
            .orderBy(paymentJpaEntity.createdAt.asc(), paymentJpaEntity.id.asc())
            .limit(limit)
            .fetch()
            .stream()
            .map(PaymentMapper::toDomain)
            .toList();
    }

    @Override
    public int findReconcileAttemptCount(PaymentId paymentId) {
        Integer attemptCount = queryFactory
            .select(paymentJpaEntity.reconcileAttemptCount)
            .from(paymentJpaEntity)
            .where(paymentJpaEntity.id.eq(paymentId.value()))
            .fetchOne();
        return attemptCount == null ? 0 : attemptCount;
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

    @Override
    public void saveReconcileSchedule(PaymentId paymentId, int attemptCount, LocalDateTime nextAttemptAt) {
        queryFactory
            .update(paymentJpaEntity)
            .set(paymentJpaEntity.reconcileAttemptCount, attemptCount)
            .set(paymentJpaEntity.reconcileNextAt, nextAttemptAt)
            .where(paymentJpaEntity.id.eq(paymentId.value()))
            .execute();
    }

    @Override
    public boolean saveReconcileScheduleIfDue(
        PaymentId paymentId, int expectedAttemptCount, LocalDateTime dueAt, LocalDateTime nextAttemptAt
    ) {
        long updated = queryFactory
            .update(paymentJpaEntity)
            .set(paymentJpaEntity.reconcileAttemptCount, expectedAttemptCount + 1)
            .set(paymentJpaEntity.reconcileNextAt, nextAttemptAt)
            .where(
                paymentJpaEntity.id.eq(paymentId.value()),
                paymentJpaEntity.reconcileAttemptCount.eq(expectedAttemptCount),
                paymentJpaEntity.reconcileNextAt.isNull().or(paymentJpaEntity.reconcileNextAt.loe(dueAt))
            )
            .execute();
        return updated == 1;
    }
}
