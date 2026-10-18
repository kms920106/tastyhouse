package com.tastyhouse.infrastructure.jpa.payment.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
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
import com.tastyhouse.domain.payment.model.PgProvider;
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
class PaymentConcurrencyIntegrationTest {

    private static final long TIMEOUT_SECONDS = 10;
    private static final long NON_BLOCKING_SECONDS = 5;

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    @Autowired
    private PaymentLoadPort paymentLoadPort;

    @Autowired
    private PaymentSavePort paymentSavePort;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbc;

    private TransactionTemplate tx;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM PAYMENT WHERE id > 0");
        tx = new TransactionTemplate(transactionManager);
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("승인 반영이 결제 행을 쥔 동안 실패 반영의 잠금 조회는 기다렸다가 COMPLETED를 읽고 실패로 덮지 않는다")
    void failReflectionWaitsForApprovalAndDoesNotOverwrite() throws Exception {
        String pgOrderId = "pg-order-approve";
        insertPayment(301L, pgOrderId);
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Future<?> approval = executor.submit(() -> tx.executeWithoutResult(status -> {
            Payment payment = paymentLoadPort.findByPgOrderIdForUpdate(pgOrderId).orElseThrow();
            acquired.countDown();
            awaitQuietly(release);
            payment.complete("pg-tid-1", LocalDateTime.now(), null);
            paymentSavePort.save(payment);
        }));
        assertThat(acquired.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        Future<PaymentStatus> failure = executor.submit(() -> tx.execute(status -> {
            Payment payment = paymentLoadPort.findByPgOrderIdForUpdate(pgOrderId).orElseThrow();
            PaymentStatus observed = payment.getPaymentStatus();
            if (observed == PaymentStatus.PENDING) {
                payment.fail();
                paymentSavePort.save(payment);
            }
            return observed;
        }));

        try {
            Thread.sleep(500);
            assertThat(failure.isDone()).isFalse();
        } finally {
            release.countDown();
        }
        approval.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(failure.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(statusOf(pgOrderId)).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("취소 반영이 결제 행을 PK 잠금으로 쥔 동안 PG 시도 기록의 잠금 조회는 기다렸다가 CANCELLED를 읽고 PG를 기록하지 않는다")
    void attemptRecordWaitsForCancellationAndSkipsCancelledPayment() throws Exception {
        String pgOrderId = "pg-order-cancel";
        insertPayment(305L, pgOrderId);
        PaymentId paymentId = PaymentId.of(idOf(pgOrderId));
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Future<?> cancellation = executor.submit(() -> tx.executeWithoutResult(status -> {
            Payment payment = paymentLoadPort.findByIdForUpdate(paymentId).orElseThrow();
            acquired.countDown();
            awaitQuietly(release);
            if (payment.getPaymentStatus() == PaymentStatus.PENDING && payment.getPgProvider() == null) {
                payment.cancel("고객 변심", LocalDateTime.now());
                paymentSavePort.save(payment);
            }
        }));
        assertThat(acquired.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        Future<PaymentStatus> attempt = executor.submit(() -> tx.execute(status -> {
            Payment payment = paymentLoadPort.findByPgOrderIdForUpdate(pgOrderId).orElseThrow();
            PaymentStatus observed = payment.getPaymentStatus();
            if (observed == PaymentStatus.PENDING) {
                payment.recordPgAttempt(PgProvider.TOSS);
                paymentSavePort.save(payment);
            }
            return observed;
        }));

        try {
            Thread.sleep(500);
            assertThat(attempt.isDone()).isFalse();
        } finally {
            release.countDown();
        }
        cancellation.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(attempt.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(statusOf(pgOrderId)).isEqualTo("CANCELLED");
        assertThat(pgProviderOf(pgOrderId)).isNull();
    }

    @Test
    @DisplayName("한 결제를 잠가도 다른 결제의 잠금 조회는 기다리지 않는다")
    void lockingOnePaymentDoesNotBlockAnother() throws Exception {
        String lockedPgOrderId = "pg-order-locked";
        String otherPgOrderId = "pg-order-other";
        insertPayment(302L, lockedPgOrderId);
        insertPayment(303L, otherPgOrderId);
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Future<?> holder = executor.submit(() -> tx.executeWithoutResult(status -> {
            paymentLoadPort.findByPgOrderIdForUpdate(lockedPgOrderId).orElseThrow();
            acquired.countDown();
            awaitQuietly(release);
        }));
        assertThat(acquired.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        try {
            Future<PaymentStatus> other = executor.submit(() -> tx.execute(status ->
                paymentLoadPort.findByPgOrderIdForUpdate(otherPgOrderId).orElseThrow().getPaymentStatus()));

            assertThat(other.get(NON_BLOCKING_SECONDS, TimeUnit.SECONDS)).isEqualTo(PaymentStatus.PENDING);
        } finally {
            release.countDown();
        }
        holder.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("없는 PG 주문번호의 잠금 조회는 빈 결과이고 다른 결제 생성을 막지 않는다")
    void lockingMissingPgOrderIdDoesNotBlockInsert() throws Exception {
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Future<Optional<Payment>> holder = executor.submit(() -> tx.execute(status -> {
            Optional<Payment> missing = paymentLoadPort.findByPgOrderIdForUpdate("pg-order-missing");
            acquired.countDown();
            awaitQuietly(release);
            return missing;
        }));
        assertThat(acquired.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        try {
            Future<?> insert = executor.submit(() -> insertPayment(304L, "pg-order-new"));

            insert.get(NON_BLOCKING_SECONDS, TimeUnit.SECONDS);
        } finally {
            release.countDown();
        }
        assertThat(holder.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEmpty();
        assertThat(statusOf("pg-order-new")).isEqualTo("PENDING");
    }

    private void insertPayment(long orderId, String pgOrderId) {
        jdbc.update("""
            INSERT INTO PAYMENT (order_id, payment_method, payment_status, amount, pg_order_id, created_at, updated_at)
            VALUES (?, 'CREDIT_CARD', 'PENDING', 21000, ?, NOW(), NOW())
            """, orderId, pgOrderId);
    }

    private long idOf(String pgOrderId) {
        return jdbc.queryForObject("SELECT id FROM PAYMENT WHERE pg_order_id = ?", Long.class, pgOrderId);
    }

    private String pgProviderOf(String pgOrderId) {
        return jdbc.queryForObject("SELECT pg_provider FROM PAYMENT WHERE pg_order_id = ?", String.class, pgOrderId);
    }

    private String statusOf(String pgOrderId) {
        return jdbc.queryForObject("SELECT payment_status FROM PAYMENT WHERE pg_order_id = ?", String.class, pgOrderId);
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("래치 대기 시간이 초과됐습니다.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
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
