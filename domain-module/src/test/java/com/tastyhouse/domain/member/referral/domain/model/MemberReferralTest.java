package com.tastyhouse.domain.member.referral.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.referral.domain.vo.ReferralId;
import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberReferralTest {

    @Test
    @DisplayName("register로 생성하면 미영속 상태(식별자·감사시각 없음)이고 PENDING 상태다")
    void register_createsTransientPendingReferral() {
        MemberId referrerId = MemberId.of(1L);
        MemberId refereeId = MemberId.of(2L);

        MemberReferral referral = MemberReferral.register(referrerId, refereeId);

        assertThat(referral.getId()).isNull();
        assertThat(referral.getReferrerId()).isEqualTo(referrerId);
        assertThat(referral.getRefereeId()).isEqualTo(refereeId);
        assertThat(referral.getStatus()).isEqualTo(MemberReferralStatus.PENDING);
        assertThat(referral.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("reward는 PENDING 상태를 REWARDED로 전이한다")
    void reward_transitionsToRewarded() {
        MemberReferral referral = MemberReferral.register(MemberId.of(1L), MemberId.of(2L));

        referral.reward();

        assertThat(referral.getStatus()).isEqualTo(MemberReferralStatus.REWARDED);
    }

    @Test
    @DisplayName("cancel은 PENDING 상태를 CANCELLED로 전이한다")
    void cancel_transitionsToCancelled() {
        MemberReferral referral = MemberReferral.register(MemberId.of(1L), MemberId.of(2L));

        referral.cancel();

        assertThat(referral.getStatus()).isEqualTo(MemberReferralStatus.CANCELLED);
    }

    @Test
    @DisplayName("PENDING이 아닌 상태에서 reward를 호출하면 BusinessException이 발생한다")
    void reward_onNonPendingStatus_throws() {
        MemberReferral referral = MemberReferral.register(MemberId.of(1L), MemberId.of(2L));
        referral.reward();

        assertThatThrownBy(referral::reward)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("PENDING이 아닌 상태에서 cancel을 호출하면 BusinessException이 발생한다")
    void cancel_onNonPendingStatus_throws() {
        MemberReferral referral = MemberReferral.register(MemberId.of(1L), MemberId.of(2L));
        referral.cancel();

        assertThatThrownBy(referral::cancel)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·상태·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        MemberReferral referral = MemberReferral.reconstitute(
            10L, MemberId.of(1L), MemberId.of(2L), MemberReferralStatus.REWARDED, createdAt
        );

        assertThat(referral.getId()).isEqualTo(10L);
        assertThat(referral.getReferralId()).isEqualTo(new ReferralId(10L));
        assertThat(referral.getStatus()).isEqualTo(MemberReferralStatus.REWARDED);
        assertThat(referral.getCreatedAt()).isEqualTo(createdAt);
    }
}
