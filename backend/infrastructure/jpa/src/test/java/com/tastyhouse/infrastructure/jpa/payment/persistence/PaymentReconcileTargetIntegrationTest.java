package com.tastyhouse.infrastructure.jpa.payment.persistence;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Testcontainers
@DataJpaTest(properties = {
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=file:${tastyhouse.schema}",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PaymentPersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentReconcileTargetIntegrationTest {

    private static final LocalDateTime FROM = LocalDateTime.of(2026, 10, 11, 9, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 10, 18, 8, 50);
    private static final LocalDateTime DUE_AT = LocalDateTime.of(2026, 10, 18, 9, 0);

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    @Autowired
    private PaymentLoadPort paymentLoadPort;

    @Autowired
    private PaymentSavePort paymentSavePort;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM PAYMENT WHERE id > 0");
    }

    @Test
    @DisplayName("PG 시도가 기록된 대상 상태 결제만 [from, to) 구간에서 생성 순으로 고른다")
    void selectsPgAttemptedPaymentsWithinHalfOpenWindow() {
        insertPayment(401L, "pg-at-from", "PENDING", "TOSS", FROM);
        insertPayment(402L, "pg-middle", "PENDING", "TOSS", FROM.plusDays(3));
        insertPayment(403L, "pg-at-to", "PENDING", "TOSS", TO);
        insertPayment(404L, "pg-before-from", "PENDING", "TOSS", FROM.minusSeconds(1));
        insertPayment(405L, "pg-no-provider", "PENDING", null, FROM.plusDays(1));
        insertPayment(406L, "pg-failed", "FAILED", "TOSS", FROM.plusDays(1));
        insertPayment(407L, "pg-completed", "COMPLETED", "TOSS", FROM.plusDays(1));

        List<Payment> targets = paymentLoadPort.findAllPgAttemptedReconcileDueByStatusCreatedBetween(PaymentStatus.PENDING, FROM, TO, DUE_AT, 100);

        assertThat(targets).extracting(Payment::getPgOrderId).containsExactly("pg-at-from", "pg-middle");
    }

    @Test
    @DisplayName("대상이 limit을 넘으면 오래된 것부터 limit건만 돌려준다")
    void limitsToOldestPayments() {
        insertPayment(411L, "pg-third", "PENDING", "TOSS", FROM.plusHours(3));
        insertPayment(412L, "pg-first", "PENDING", "TOSS", FROM.plusHours(1));
        insertPayment(413L, "pg-second", "PENDING", "TOSS", FROM.plusHours(2));

        List<Payment> targets = paymentLoadPort.findAllPgAttemptedReconcileDueByStatusCreatedBetween(PaymentStatus.PENDING, FROM, TO, DUE_AT, 2);

        assertThat(targets).extracting(Payment::getPgOrderId).containsExactly("pg-first", "pg-second");
    }

    @Test
    @DisplayName("다음 대사 시각이 비었거나 dueAt 이하인 결제만 고르고, dueAt 이후로 미뤄진 결제는 뺀다")
    void selectsOnlyPaymentsWhoseReconcileTurnHasCome() {
        long neverTried = insertPayment(421L, "pg-never-tried", "PENDING", "TOSS", FROM.plusHours(1));
        long dueNow = insertPayment(422L, "pg-due-now", "PENDING", "TOSS", FROM.plusHours(2));
        long deferred = insertPayment(423L, "pg-deferred", "PENDING", "TOSS", FROM.plusHours(3));
        jdbc.update("UPDATE PAYMENT SET reconcile_next_at = ? WHERE id = ?", DUE_AT, dueNow);
        jdbc.update("UPDATE PAYMENT SET reconcile_next_at = ? WHERE id = ?", DUE_AT.plusSeconds(1), deferred);

        List<Payment> targets = paymentLoadPort.findAllPgAttemptedReconcileDueByStatusCreatedBetween(PaymentStatus.PENDING, FROM, TO, DUE_AT, 100);

        assertThat(targets).extracting(Payment::getId).containsExactly(neverTried, dueNow);
    }

    @Test
    @DisplayName("같은 트랜잭션에서 결제를 읽은 뒤 대사 일정을 저장하고 결제를 저장해도, 엔티티 저장이 두 컬럼을 옛 값으로 덮지 않는다")
    void savesReconcileScheduleAndEntitySaveKeepsIt() {
        long paymentId = insertPayment(431L, "pg-schedule", "PENDING", "TOSS", FROM.plusHours(1));
        LocalDateTime nextAt = DUE_AT.plusMinutes(39);
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThat(paymentLoadPort.findReconcileAttemptCount(PaymentId.of(paymentId))).isZero();
        tx.executeWithoutResult(status -> {
            Payment payment = paymentLoadPort.findByPgOrderIdForUpdate("pg-schedule").orElseThrow();
            paymentSavePort.saveReconcileSchedule(PaymentId.of(paymentId), 3, nextAt);
            payment.fail();
            paymentSavePort.save(payment);
        });

        assertThat(paymentLoadPort.findReconcileAttemptCount(PaymentId.of(paymentId))).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT reconcile_next_at FROM PAYMENT WHERE id = ?", LocalDateTime.class, paymentId))
            .isEqualTo(nextAt);
        assertThat(jdbc.queryForObject("SELECT payment_status FROM PAYMENT WHERE id = ?", String.class, paymentId))
            .isEqualTo("FAILED");
    }

    @Test
    @DisplayName("조건부 일정 저장은 시도 횟수가 읽은 값 그대로이고 차례가 됐을 때만 횟수를 하나 올리고 다음 시각을 저장한다")
    void saveReconcileScheduleIfDue_claimsOnlyWhenCountAndTurnMatch() {
        long paymentId = insertPayment(441L, "pg-claim", "PENDING", "TOSS", FROM.plusHours(1));
        jdbc.update("UPDATE PAYMENT SET reconcile_attempt_count = 2, reconcile_next_at = ? WHERE id = ?", DUE_AT, paymentId);
        LocalDateTime nextAt = DUE_AT.plusMinutes(39);

        boolean claimed = claim(PaymentId.of(paymentId), 2, nextAt);

        assertThat(claimed).isTrue();
        assertThat(paymentLoadPort.findReconcileAttemptCount(PaymentId.of(paymentId))).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT reconcile_next_at FROM PAYMENT WHERE id = ?", LocalDateTime.class, paymentId))
            .isEqualTo(nextAt);
    }

    @Test
    @DisplayName("같은 결제를 두 실행이 같은 시도 횟수로 선점하면 한 쪽만 성공하고 횟수는 한 번만 오른다")
    void saveReconcileScheduleIfDue_secondClaimWithSameCountFails() {
        long paymentId = insertPayment(442L, "pg-double-claim", "PENDING", "TOSS", FROM.plusHours(1));
        LocalDateTime nextAt = DUE_AT.plusMinutes(9);

        boolean first = claim(PaymentId.of(paymentId), 0, nextAt);
        boolean second = claim(PaymentId.of(paymentId), 0, nextAt);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(paymentLoadPort.findReconcileAttemptCount(PaymentId.of(paymentId))).isEqualTo(1);
    }

    @Test
    @DisplayName("다음 대사 시각이 dueAt 이후로 미뤄져 있으면(web 승인 시도가 일정을 초기화함) 횟수가 같아도 선점하지 않는다")
    void saveReconcileScheduleIfDue_doesNotClaimDeferredPayment() {
        long paymentId = insertPayment(443L, "pg-deferred-claim", "PENDING", "TOSS", FROM.plusHours(1));
        LocalDateTime resetNextAt = DUE_AT.plusMinutes(10);
        jdbc.update("UPDATE PAYMENT SET reconcile_attempt_count = 0, reconcile_next_at = ? WHERE id = ?", resetNextAt, paymentId);

        boolean claimed = claim(PaymentId.of(paymentId), 0, DUE_AT.plusMinutes(9));

        assertThat(claimed).isFalse();
        assertThat(jdbc.queryForObject("SELECT reconcile_next_at FROM PAYMENT WHERE id = ?", LocalDateTime.class, paymentId))
            .isEqualTo(resetNextAt);
    }

    private boolean claim(PaymentId paymentId, int expectedAttemptCount, LocalDateTime nextAttemptAt) {
        Boolean claimed = new TransactionTemplate(transactionManager).execute(status ->
            paymentSavePort.saveReconcileScheduleIfDue(paymentId, expectedAttemptCount, DUE_AT, nextAttemptAt)
        );
        return Boolean.TRUE.equals(claimed);
    }

    private long insertPayment(long orderId, String pgOrderId, String paymentStatus, String pgProvider, LocalDateTime createdAt) {
        jdbc.update("""
            INSERT INTO PAYMENT (order_id, payment_method, payment_status, amount, pg_provider, pg_order_id, created_at, updated_at)
            VALUES (?, 'CREDIT_CARD', ?, 21000, ?, ?, ?, ?)
            """, orderId, paymentStatus, pgProvider, pgOrderId, createdAt, createdAt);
        return jdbc.queryForObject("SELECT id FROM PAYMENT WHERE order_id = ?", Long.class, orderId);
    }

    @SpringBootConfiguration
    @EnableJpaAuditing
    @EntityScan(basePackageClasses = PaymentJpaEntity.class)
    @EnableJpaRepositories(basePackageClasses = PaymentJpaRepository.class)
    static class PaymentPersistenceTestConfig {

        @Bean
        JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }
    }
}
