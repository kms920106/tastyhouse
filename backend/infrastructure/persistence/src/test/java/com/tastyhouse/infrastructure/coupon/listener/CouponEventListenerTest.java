package com.tastyhouse.infrastructure.coupon.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.coupon.event.MemberCouponIssuedEvent;
import com.tastyhouse.domain.coupon.event.MemberCouponUsedEvent;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CouponEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>스프링 컨텍스트 없이 리스너를 직접 생성해 핸들러를 호출한다 — AFTER_COMMIT 발화 자체는 프레임워크
 * 몫이라 검증 대상이 아니다. 이 리스너는 협력자 없이 기록만 하므로, 무엇이 기록되는지를
 * {@link ListenerLogCapture}로 확인한다.
 */
class CouponEventListenerTest {

    private final CouponEventListener listener = new CouponEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(CouponEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("쿠폰 발급 이벤트를 받으면 발급 식별자·회원·쿠폰·발급시각을 기록한다")
    void logsIssuedEvent() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 4, 1, 10, 30);
        MemberCouponIssuedEvent event = new MemberCouponIssuedEvent(
            MemberCouponId.of(11L),
            MemberId.of(22L),
            CouponId.of(33L),
            issuedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("쿠폰 발급 완료")
            .contains("11")
            .contains("22")
            .contains("33")
            .contains(issuedAt.toString());
    }

    @Test
    @DisplayName("쿠폰 사용 이벤트를 받으면 사용 식별자·회원·쿠폰·사용시각을 기록한다")
    void logsUsedEvent() {
        LocalDateTime usedAt = LocalDateTime.of(2026, 4, 2, 19, 5);
        MemberCouponUsedEvent event = new MemberCouponUsedEvent(
            MemberCouponId.of(44L),
            MemberId.of(55L),
            CouponId.of(66L),
            usedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("쿠폰 사용 완료")
            .contains("44")
            .contains("55")
            .contains("66")
            .contains(usedAt.toString());
    }
}
