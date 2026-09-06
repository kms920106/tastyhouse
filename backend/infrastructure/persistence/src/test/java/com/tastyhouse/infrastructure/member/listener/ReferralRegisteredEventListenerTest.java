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
