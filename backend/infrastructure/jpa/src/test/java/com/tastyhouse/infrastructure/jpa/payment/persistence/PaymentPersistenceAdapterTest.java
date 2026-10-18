package com.tastyhouse.infrastructure.jpa.payment.persistence;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.vo.PaymentId;

import static com.tastyhouse.infrastructure.jpa.payment.persistence.QPaymentJpaEntity.paymentJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentPersistenceAdapterTest {

    private final JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
    private final PaymentJpaRepository paymentJpaRepository = mock(PaymentJpaRepository.class);
    private final JPAQuery<Long> idQuery = mock();
    private final JPAQuery<PaymentJpaEntity> entityQuery = mock();

    private final PaymentPersistenceAdapter adapter = new PaymentPersistenceAdapter(queryFactory, paymentJpaRepository);

    @BeforeEach
    void setUp() {
        when(queryFactory.select(paymentJpaEntity.id)).thenReturn(idQuery);
        when(idQuery.from(paymentJpaEntity)).thenReturn(idQuery);
        when(idQuery.where(any(Predicate.class))).thenReturn(idQuery);
        when(queryFactory.selectFrom(paymentJpaEntity)).thenReturn(entityQuery);
        when(entityQuery.where(any(Predicate.class))).thenReturn(entityQuery);
        when(entityQuery.setLockMode(any(LockModeType.class))).thenReturn(entityQuery);
    }

    @Test
    @DisplayName("PG 주문번호 잠금 조회는 id를 잠금 없이 찾은 뒤 그 PK 행만 PESSIMISTIC_WRITE로 잠가 읽는다")
    void locksPaymentRowByPrimaryKey() {
        when(idQuery.fetchOne()).thenReturn(200L);
        when(entityQuery.fetchOne()).thenReturn(null);

        adapter.findByPgOrderIdForUpdate("pg-order-1");

        var ordered = inOrder(idQuery, entityQuery);
        ordered.verify(idQuery).fetchOne();
        ordered.verify(entityQuery).setLockMode(LockModeType.PESSIMISTIC_WRITE);
        ordered.verify(entityQuery).fetchOne();
        verify(idQuery, never()).setLockMode(any(LockModeType.class));
        verify(entityQuery).where(paymentJpaEntity.id.eq(200L));
    }

    @Test
    @DisplayName("PG 주문번호에 해당하는 결제가 없으면 잠금 조회 없이 빈 Optional을 돌려준다")
    void returnsEmptyWithoutLockingWhenAbsent() {
        when(idQuery.fetchOne()).thenReturn(null);

        assertThat(adapter.findByPgOrderIdForUpdate("pg-order-1")).isEmpty();
        verify(queryFactory, never()).selectFrom(paymentJpaEntity);
    }

    @Test
    @DisplayName("미확정 결제 대사 대상 조회는 상태 일치·PG 시도 기록·[from, to) 구간·대사 차례 도래로 거르고 생성 순으로 limit건만 읽는다")
    void findsPgAttemptedPaymentsByStatusWithinHalfOpenWindow() {
        LocalDateTime from = LocalDateTime.of(2026, 10, 11, 9, 0);
        LocalDateTime to = LocalDateTime.of(2026, 10, 18, 8, 50);
        LocalDateTime dueAt = LocalDateTime.of(2026, 10, 18, 9, 0);
        when(entityQuery.where(any(Predicate[].class))).thenReturn(entityQuery);
        when(entityQuery.orderBy(any(OrderSpecifier[].class))).thenReturn(entityQuery);
        when(entityQuery.limit(anyLong())).thenReturn(entityQuery);
        when(entityQuery.fetch()).thenReturn(List.of());

        assertThat(adapter.findAllPgAttemptedReconcileDueByStatusCreatedBetween(PaymentStatus.PENDING, from, to, dueAt, 100))
            .isEmpty();

        verify(entityQuery).where(
            paymentJpaEntity.paymentStatus.eq("PENDING"),
            paymentJpaEntity.pgProvider.isNotNull(),
            paymentJpaEntity.createdAt.goe(from),
            paymentJpaEntity.createdAt.lt(to),
            paymentJpaEntity.reconcileNextAt.isNull().or(paymentJpaEntity.reconcileNextAt.loe(dueAt))
        );
        verify(entityQuery).orderBy(paymentJpaEntity.createdAt.asc(), paymentJpaEntity.id.asc());
        verify(entityQuery).limit(100L);
        verify(entityQuery, never()).setLockMode(any(LockModeType.class));
    }

    @Test
    @DisplayName("대사 시도 횟수 조회는 결제 행이 없으면 0을 돌려준다")
    void returnsZeroAttemptCountWhenAbsent() {
        JPAQuery<Integer> countQuery = mock();
        when(queryFactory.select(paymentJpaEntity.reconcileAttemptCount)).thenReturn(countQuery);
        when(countQuery.from(paymentJpaEntity)).thenReturn(countQuery);
        when(countQuery.where(any(Predicate.class))).thenReturn(countQuery);
        when(countQuery.fetchOne()).thenReturn(null);

        assertThat(adapter.findReconcileAttemptCount(PaymentId.of(200L))).isZero();
        verify(countQuery).where(paymentJpaEntity.id.eq(200L));
    }

    @Test
    @DisplayName("대사 일정 저장은 그 결제 행의 시도 횟수와 다음 시각만 UPDATE한다")
    void updatesReconcileScheduleColumnsOnly() {
        JPAUpdateClause update = mock();
        LocalDateTime nextAt = LocalDateTime.of(2026, 10, 18, 9, 39);
        when(queryFactory.update(paymentJpaEntity)).thenReturn(update);
        when(update.set(paymentJpaEntity.reconcileAttemptCount, 3)).thenReturn(update);
        when(update.set(paymentJpaEntity.reconcileNextAt, nextAt)).thenReturn(update);
        when(update.where(any(Predicate.class))).thenReturn(update);

        adapter.saveReconcileSchedule(PaymentId.of(200L), 3, nextAt);

        verify(update).set(paymentJpaEntity.reconcileAttemptCount, 3);
        verify(update).set(paymentJpaEntity.reconcileNextAt, nextAt);
        verify(update).where(paymentJpaEntity.id.eq(200L));
        verify(update).execute();
    }

    @Test
    @DisplayName("대사 차례 선점은 시도 횟수가 읽은 값 그대로이고 다음 시각이 비었거나 dueAt 이하일 때만 횟수를 하나 올리고 다음 시각을 저장한다")
    void claimsReconcileTurnOnlyWhenCountAndTurnMatch() {
        JPAUpdateClause update = mock();
        LocalDateTime dueAt = LocalDateTime.of(2026, 10, 18, 9, 0);
        LocalDateTime nextAt = LocalDateTime.of(2026, 10, 18, 10, 19);
        when(queryFactory.update(paymentJpaEntity)).thenReturn(update);
        when(update.set(paymentJpaEntity.reconcileAttemptCount, 4)).thenReturn(update);
        when(update.set(paymentJpaEntity.reconcileNextAt, nextAt)).thenReturn(update);
        when(update.where(any(Predicate[].class))).thenReturn(update);
        when(update.execute()).thenReturn(1L);

        assertThat(adapter.saveReconcileScheduleIfDue(PaymentId.of(200L), 3, dueAt, nextAt)).isTrue();

        verify(update).set(paymentJpaEntity.reconcileAttemptCount, 4);
        verify(update).set(paymentJpaEntity.reconcileNextAt, nextAt);
        verify(update).where(
            paymentJpaEntity.id.eq(200L),
            paymentJpaEntity.reconcileAttemptCount.eq(3),
            paymentJpaEntity.reconcileNextAt.isNull().or(paymentJpaEntity.reconcileNextAt.loe(dueAt))
        );
    }

    @Test
    @DisplayName("대사 차례 선점 UPDATE가 0행이면(다른 실행이 먼저 가져갔거나 차례가 아님) 선점 실패로 false를 돌려준다")
    void returnsFalseWhenClaimUpdatesNoRow() {
        JPAUpdateClause update = mock();
        LocalDateTime dueAt = LocalDateTime.of(2026, 10, 18, 9, 0);
        LocalDateTime nextAt = LocalDateTime.of(2026, 10, 18, 10, 19);
        when(queryFactory.update(paymentJpaEntity)).thenReturn(update);
        when(update.set(paymentJpaEntity.reconcileAttemptCount, 4)).thenReturn(update);
        when(update.set(paymentJpaEntity.reconcileNextAt, nextAt)).thenReturn(update);
        when(update.where(any(Predicate[].class))).thenReturn(update);
        when(update.execute()).thenReturn(0L);

        assertThat(adapter.saveReconcileScheduleIfDue(PaymentId.of(200L), 3, dueAt, nextAt)).isFalse();
    }
}
