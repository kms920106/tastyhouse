package com.tastyhouse.infrastructure.point.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.event.PointEarnedEvent;
import com.tastyhouse.domain.point.event.PointRefundedEvent;
import com.tastyhouse.domain.point.event.PointUsedEvent;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PointEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p><b>이 리스너가 잔액을 건드리지 않는 것이 정상</b>이라는 점을 함께 고정한다 — 포인트 증감은
 * 도메인 서비스 {@code PointLedgerService}가 이벤트 발행 <b>이전에</b> 이미 수행했고, 이 리스너는 그
 * 사실을 기록만 한다. 협력자를 주입받지 않는 생성자가 그 증거이며, 여기에 원장 서비스가 추가되면
 * 같은 금액이 두 번 반영되므로 이 테스트가 컴파일되지 않아 드러난다.
 */
class PointEventListenerTest {

    private final PointEventListener listener = new PointEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(PointEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("포인트 적립 이벤트를 받으면 회원·금액·사유·적립 시각을 기록한다")
    void logsEarnedEvent() {
        LocalDateTime earnedAt = LocalDateTime.of(2026, 4, 9, 13, 10);
        PointEarnedEvent event = new PointEarnedEvent(
            MemberId.of(301L),
            1500,
            "현장 현금 결제 적립 (10%)",
            earnedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("포인트 적립 완료")
            .contains("301")
            .contains("1500")
            .contains("현장 현금 결제 적립 (10%)")
            .contains(earnedAt.toString());
    }

    @Test
    @DisplayName("포인트 사용 이벤트를 받으면 회원·금액·사용 시각을 기록한다")
    void logsUsedEvent() {
        LocalDateTime usedAt = LocalDateTime.of(2026, 4, 10, 18, 25);
        PointUsedEvent event = new PointUsedEvent(MemberId.of(302L), 700, usedAt);

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("포인트 사용 완료")
            .contains("302")
            .contains("700")
            .contains(usedAt.toString());
    }

    @Test
    @DisplayName("포인트 환불 이벤트를 받으면 회원·금액·환불 시각을 기록한다")
    void logsRefundedEvent() {
        LocalDateTime refundedAt = LocalDateTime.of(2026, 4, 11, 20, 5);
        PointRefundedEvent event = new PointRefundedEvent(MemberId.of(303L), 700, refundedAt);

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("포인트 환불 완료")
            .contains("303")
            .contains("700")
            .contains(refundedAt.toString());
    }
}
