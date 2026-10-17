package com.tastyhouse.infrastructure.jpa.point.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.application.point.port.out.write.PointHistorySavePort;
import com.tastyhouse.application.point.port.out.write.PointLoadPort;
import com.tastyhouse.application.point.port.out.write.PointSavePort;
import com.tastyhouse.application.point.service.PointLedgerService;

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
@Import({PointPersistenceAdapter.class, PointHistoryPersistenceAdapter.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PointConcurrencyIntegrationTest {

    private static final long TIMEOUT_SECONDS = 10;

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    @Autowired
    private PointLoadPort pointLoadPort;

    @Autowired
    private PointSavePort pointSavePort;

    @Autowired
    private PointHistorySavePort pointHistorySavePort;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbc;

    private TransactionTemplate tx;
    private PointLedgerService ledger;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM POINT_HISTORY");
        jdbc.update("DELETE FROM POINT");
        tx = new TransactionTemplate(transactionManager);
        ledger = new PointLedgerService(pointLoadPort, pointSavePort, pointHistorySavePort, event -> {
        });
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    @DisplayName("잔액 1,000P 회원의 1,000P 사용 2건이 동시에 오면 1건만 성공하고 나머지는 POINT_INSUFFICIENT다")
    void concurrentUsesAreSerialized() throws Exception {
        long memberId = 101L;
        insertPoint(memberId, 1000);
        CountDownLatch start = new CountDownLatch(1);

        Future<Throwable> first = executor.submit(captureFailure(start, () -> ledger.usePoints(MemberId.of(memberId), 1000)));
        Future<Throwable> second = executor.submit(captureFailure(start, () -> ledger.usePoints(MemberId.of(memberId), 1000)));
        start.countDown();

        List<Throwable> failures = new ArrayList<>();
        addIfPresent(failures, first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        addIfPresent(failures, second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        assertThat(failures).singleElement()
            .isInstanceOf(DomainException.class)
            .hasFieldOrPropertyWithValue("errorCode", DomainErrorCode.POINT_INSUFFICIENT);
        assertThat(balanceOf(memberId)).isZero();
        assertThat(historyCountOf(memberId)).isEqualTo(1);
    }

    @Test
    @DisplayName("잠금 로드로 행을 쥔 트랜잭션이 끝날 때까지 다른 잠금 로드는 기다렸다가 갱신된 잔액을 읽는다")
    void lockingLoadWaitsForHolderAndReadsCommittedBalance() throws Exception {
        long memberId = 102L;
        insertPoint(memberId, 1000);
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        Future<?> holder = executor.submit(() -> tx.executeWithoutResult(status -> {
            Point point = pointLoadPort.findByMemberIdForUpdate(MemberId.of(memberId)).orElseThrow();
            acquired.countDown();
            awaitQuietly(release);
            point.deductPoints(300);
            pointSavePort.save(point);
        }));
        assertThat(acquired.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        Future<Integer> waiter = executor.submit(() -> tx.execute(status ->
            pointLoadPort.findByMemberIdForUpdate(MemberId.of(memberId)).orElseThrow().getAvailablePoints()));

        Thread.sleep(500);
        assertThat(waiter.isDone()).isFalse();

        release.countDown();
        holder.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(waiter.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(700);
    }

    @Test
    @DisplayName("포인트 행이 없는 같은 회원에게 첫 적립 2건이 동시에 와도 행 1개에 둘 다 반영된다")
    void concurrentFirstEarnsForSameMemberAreBothApplied() throws Exception {
        long memberId = 103L;
        CountDownLatch start = new CountDownLatch(1);

        Future<Throwable> first = executor.submit(captureFailure(start, () -> ledger.earnPoints(MemberId.of(memberId), 1000, "추천받기 보상")));
        Future<Throwable> second = executor.submit(captureFailure(start, () -> ledger.earnPoints(MemberId.of(memberId), 500, "현장 결제 적립")));
        start.countDown();

        assertThat(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isNull();
        assertThat(second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM POINT WHERE member_id = ?", Integer.class, memberId))
            .isEqualTo(1);
        assertThat(balanceOf(memberId)).isEqualTo(1500);
        assertThat(historyCountOf(memberId)).isEqualTo(2);
    }

    @Test
    @DisplayName("포인트 행이 없는 서로 다른 회원 2명의 첫 적립이 동시에 와도 교착 없이 둘 다 반영된다")
    void concurrentFirstEarnsForDifferentMembersDoNotDeadlock() throws Exception {
        long firstMemberId = 201L;
        long secondMemberId = 202L;
        CountDownLatch start = new CountDownLatch(1);

        Future<Throwable> first = executor.submit(captureFailure(start, () -> ledger.earnPoints(MemberId.of(firstMemberId), 1000, "추천하기 보상")));
        Future<Throwable> second = executor.submit(captureFailure(start, () -> ledger.earnPoints(MemberId.of(secondMemberId), 500, "추천받기 보상")));
        start.countDown();

        assertThat(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isNull();
        assertThat(second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isNull();
        assertThat(balanceOf(firstMemberId)).isEqualTo(1000);
        assertThat(balanceOf(secondMemberId)).isEqualTo(500);
    }

    @Test
    @DisplayName("잔액 잠금 조회는 member_id UNIQUE 인덱스를 쓰고 중복 비유니크 인덱스는 없다")
    void lockingLoadUsesUniqueMemberIdIndex() {
        long memberId = 104L;
        insertPoint(memberId, 0);

        List<Map<String, Object>> plan = jdbc.queryForList(
            "EXPLAIN SELECT * FROM POINT WHERE member_id = ? FOR UPDATE", memberId);
        List<String> indexNames = jdbc.queryForList("SHOW INDEX FROM POINT").stream()
            .map(row -> (String) row.get("Key_name"))
            .toList();

        assertThat(plan).singleElement().satisfies(row -> assertThat(row.get("key")).isEqualTo("member_id"));
        assertThat(indexNames).contains("member_id").doesNotContain("idx_point_member_id");
    }

    private Callable<Throwable> captureFailure(CountDownLatch start, Runnable work) {
        return () -> {
            awaitQuietly(start);
            try {
                tx.executeWithoutResult(status -> work.run());
                return null;
            } catch (RuntimeException e) {
                return e;
            }
        };
    }

    private void insertPoint(long memberId, int balance) {
        jdbc.update("""
            INSERT INTO POINT (member_id, available_points, expired_this_month, created_at, updated_at)
            VALUES (?, ?, 0, NOW(), NOW())
            """, memberId, balance);
    }

    private int balanceOf(long memberId) {
        return jdbc.queryForObject("SELECT available_points FROM POINT WHERE member_id = ?", Integer.class, memberId);
    }

    private int historyCountOf(long memberId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM POINT_HISTORY WHERE member_id = ?", Integer.class, memberId);
    }

    private static void addIfPresent(List<Throwable> failures, Throwable failure) {
        if (failure != null) {
            failures.add(failure);
        }
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
    @EntityScan(basePackageClasses = PointJpaEntity.class)
    @EnableJpaRepositories(basePackageClasses = PointJpaRepository.class)
    static class PointPersistenceTestConfig {

        @Bean
        JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }
    }
}
