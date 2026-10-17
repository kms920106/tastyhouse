package com.tastyhouse.application.point.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.application.point.port.out.write.PointHistorySavePort;
import com.tastyhouse.application.point.port.out.write.PointLoadPort;
import com.tastyhouse.application.point.port.out.write.PointSavePort;
import com.tastyhouse.testsupport.review.service.FakeDomainEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointLedgerServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(7L);

    private final FakePointPersistence points = new FakePointPersistence();
    private final List<PointHistory> histories = new ArrayList<>();
    private final PointHistorySavePort historySavePort = history -> {
        histories.add(history);
        return history;
    };

    private final PointLedgerService ledger = new PointLedgerService(
        points, points, historySavePort, new FakeDomainEventPublisher());

    @Test
    @DisplayName("사용·차감·환급·회수는 잔액을 잠금 로드로 읽는다")
    void balanceChangesUseLockingLoad() {
        points.register(MEMBER_ID, 5000);

        ledger.usePoints(MEMBER_ID, 1000);
        ledger.deductPoints(MEMBER_ID, 1000, "관리자 차감");
        ledger.refundPoints(MEMBER_ID, 500);
        ledger.reclaimEarnedPoints(MEMBER_ID, 500);

        assertThat(points.calls).containsExactly(
            "lockingLoad", "lockingLoad", "lockingLoad", "lockingLoad");
        assertThat(points.memberBalance()).isEqualTo(3000);
    }

    @Test
    @DisplayName("포인트 행이 있는 회원의 적립은 행 보장 뒤 잠금 로드로 읽는다")
    void earnOnExistingRowEnsuresRowThenUsesLockingLoad() {
        points.register(MEMBER_ID, 1000);

        ledger.earnPoints(MEMBER_ID, 300, "현장 결제 적립");

        assertThat(points.calls).containsExactly("saveIfAbsent", "lockingLoad");
        assertThat(points.memberBalance()).isEqualTo(1300);
    }

    @Test
    @DisplayName("포인트 행이 없는 회원의 첫 적립은 0P 행을 보장한 뒤 잠금 로드로 읽어 적립한다")
    void firstEarnEnsuresRowThenUsesLockingLoad() {
        ledger.earnPoints(MEMBER_ID, 1000, "추천받기 보상");

        assertThat(points.calls).containsExactly("saveIfAbsent", "lockingLoad");
        assertThat(points.memberBalance()).isEqualTo(1000);
        assertThat(histories).hasSize(1);
    }

    @Test
    @DisplayName("잠금 로드로 읽은 잔액보다 많이 쓰면 POINT_INSUFFICIENT로 거부하고 이력을 남기지 않는다")
    void deductBeyondLockedBalanceIsRejected() {
        points.register(MEMBER_ID, 1000);
        ledger.usePoints(MEMBER_ID, 1000);

        assertThatThrownBy(() -> ledger.usePoints(MEMBER_ID, 1000))
            .isInstanceOf(DomainException.class)
            .hasFieldOrPropertyWithValue("errorCode", DomainErrorCode.POINT_INSUFFICIENT);
        assertThat(points.memberBalance()).isZero();
        assertThat(histories).hasSize(1);
    }

    private static final class FakePointPersistence implements PointLoadPort, PointSavePort {

        private final Map<Long, Point> rows = new HashMap<>();
        private final List<String> calls = new ArrayList<>();
        private long nextId = 1L;

        void register(MemberId memberId, int balance) {
            rows.put(memberId.value(), Point.reconstitute(nextId++, memberId, balance, 0));
        }

        int memberBalance() {
            return rows.get(MEMBER_ID.value()).getAvailablePoints();
        }

        @Override
        public Optional<Point> findByMemberIdForUpdate(MemberId memberId) {
            calls.add("lockingLoad");
            Point stored = rows.get(memberId.value());
            return Optional.ofNullable(stored).map(this::copyOf);
        }

        @Override
        public void saveIfAbsent(MemberId memberId) {
            calls.add("saveIfAbsent");
            if (!rows.containsKey(memberId.value())) {
                register(memberId, 0);
            }
        }

        @Override
        public Point save(Point point) {
            Long id = point.getId() == null ? nextId++ : point.getId();
            Point stored = Point.reconstitute(
                id, point.getMemberId(), point.getAvailablePoints(), point.getExpiredThisMonth());
            rows.put(point.getMemberId().value(), stored);
            return copyOf(stored);
        }

        private Point copyOf(Point point) {
            return Point.reconstitute(
                point.getId(), point.getMemberId(), point.getAvailablePoints(), point.getExpiredThisMonth());
        }
    }
}
