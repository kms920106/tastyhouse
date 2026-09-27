package com.tastyhouse.application.member.referral.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberReferralStateMapperTest {

    @Test
    @DisplayName("MemberReferral → MemberReferralState → MemberReferral 왕복 시 추천인·피추천인이 뒤바뀌지 않는다")
    void roundTrip() {
        MemberReferral original = MemberReferral.reconstitute(
            131L, MemberId.of(132L), MemberId.of(133L), MemberReferralStatus.REWARDED,
            LocalDateTime.of(2026, 12, 12, 12, 12));

        MemberReferral restored = MemberReferralStateMapper.toDomain(MemberReferralStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
