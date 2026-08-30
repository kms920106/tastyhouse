package com.tastyhouse.infrastructure.member.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.service.ReferralRewardCompletionService;
import com.tastyhouse.domain.member.referral.vo.ReferralId;
import com.tastyhouse.domain.point.service.PointLedgerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ReferralRegisteredEventListener}의 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>이 리스너는 referral↔point 두 컨텍스트를 잇는 지점이므로, 검증 대상이 로깅이 아니라
 * <b>적립 2건과 보상 완료 전이가 모두, 그리고 그 순서대로 일어나는가</b>이다.
 *
 * <p>추천인(referrer)과 피추천인(referee)이 둘 다 {@code MemberId}라 <b>순서를 바꿔도 컴파일된다</b> —
 * 두 값이 뒤바뀌면 보상 사유가 서로 반대로 적립되므로, 서로 다른 값을 넣어 각각이 제 자리에
 * 적립되는지 확인한다.
 */
class ReferralRegisteredEventListenerTest {

    private static final ReferralId REFERRAL_ID = new ReferralId(201L);
    private static final MemberId REFERRER_ID = MemberId.of(202L);
    private static final MemberId REFEREE_ID = MemberId.of(203L);

    private final RecordingLedger ledger = new RecordingLedger();
    private final RecordingCompletion completion = new RecordingCompletion();
    private final ReferralRegisteredEventListener listener =
        new ReferralRegisteredEventListener(ledger, completion);

    @Test
    @DisplayName("추천 등록 이벤트를 받으면 추천인·피추천인 양쪽에 각자의 사유로 1000P씩 적립한다")
    void earnsRewardForBothSides() {
        listener.handle(event());

        assertThat(ledger.earned).containsExactly(
            new Earned(REFERRER_ID, 1000, "추천인 보상"),
            new Earned(REFEREE_ID, 1000, "추천받기 보상")
        );
    }

    @Test
    @DisplayName("적립을 마친 뒤 추천 관계를 보상 완료로 전이시킨다")
    void completesRewardAfterEarning() {
        listener.handle(event());

        assertThat(completion.completed).containsExactly(REFERRAL_ID);
    }

    @Test
    @DisplayName("적립이 실패하면 보상 완료 전이를 하지 않는다")
    void doesNotCompleteWhenEarningFails() {
        ledger.failOnNextEarn = true;

        assertThatThrownBy(() -> listener.handle(event()))
            .isInstanceOf(IllegalStateException.class);

        assertThat(completion.completed)
            .as("전이가 먼저 커밋되면 '완료로 표시됐지만 포인트는 없는' 추천 관계가 남는다")
            .isEmpty();
    }

    private static ReferralRegisteredEvent event() {
        return new ReferralRegisteredEvent(
            REFERRAL_ID,
            REFERRER_ID,
            REFEREE_ID,
            LocalDateTime.of(2026, 4, 8, 12, 0)
        );
    }

    private record Earned(MemberId memberId, int amount, String reason) {
    }

    /**
     * 적립 호출만 기록하는 원장 스텁. {@code PointLedgerService}는 인터페이스가 아니라 클래스이므로
     * 상속으로 대체하며, 부모 생성자가 요구하는 포트는 호출되지 않으므로 {@code null}을 넘긴다
     * (이 테스트가 검증하는 경로는 오버라이드한 {@code earnPoints}뿐이다).
     */
    private static final class RecordingLedger extends PointLedgerService {

        private final List<Earned> earned = new ArrayList<>();
        private boolean failOnNextEarn;

        private RecordingLedger() {
            super(null, null, null);
        }

        @Override
        public void earnPoints(MemberId memberId, int pointAmount, String reason) {
            if (failOnNextEarn) {
                throw new IllegalStateException("적립 실패 시뮬레이션");
            }
            earned.add(new Earned(memberId, pointAmount, reason));
        }
    }

    /**
     * 보상 완료 전이 호출만 기록하는 스텁. 위와 같은 이유로 상속을 쓴다.
     */
    private static final class RecordingCompletion extends ReferralRewardCompletionService {

        private final List<ReferralId> completed = new ArrayList<>();

        private RecordingCompletion() {
            super(null);
        }

        @Override
        public void complete(ReferralId referralId) {
            completed.add(referralId);
        }
    }
}
