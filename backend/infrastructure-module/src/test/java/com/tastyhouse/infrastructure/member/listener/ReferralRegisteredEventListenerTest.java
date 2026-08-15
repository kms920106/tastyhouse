package com.tastyhouse.infrastructure.member.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ReferralRegisteredEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>추천인(referrer)과 피추천인(referee)이 둘 다 {@code MemberId}라 <b>순서를 바꿔도 컴파일된다</b> —
 * 기록에서 두 값이 뒤바뀌면 "누가 누구를 추천했는지"가 반대로 남으므로, 서로 다른 값을 넣어 각각이
 * 제 자리에 기록되는지 확인한다.
 */
class ReferralRegisteredEventListenerTest {

    private final ReferralRegisteredEventListener listener = new ReferralRegisteredEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(ReferralRegisteredEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("추천 등록 이벤트를 받으면 추천 식별자·추천인·피추천인·등록 시각을 순서대로 기록한다")
    void logsRegisteredEvent() {
        LocalDateTime registeredAt = LocalDateTime.of(2026, 4, 8, 12, 0);
        ReferralRegisteredEvent event = new ReferralRegisteredEvent(
            new ReferralId(201L),
            MemberId.of(202L),
            MemberId.of(203L),
            registeredAt
        );

        listener.handle(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("추천 등록 완료")
            .contains("referralId=201")
            .contains("referrerId=202")
            .contains("refereeId=203")
            .contains(registeredAt.toString());
    }
}
