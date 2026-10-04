package com.tastyhouse.infrastructure.persistence.member.referral.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.model.MemberReferralStatus;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberReferralMapperTest {

    @Test
    @DisplayName("MemberReferral → 엔티티 변환 시 추천인·피추천인이 뒤바뀌지 않는다")
    void toEntity() {
        MemberReferral original = MemberReferral.reconstitute(
            131L, MemberId.of(132L), MemberId.of(133L), MemberReferralStatus.REWARDED,
            LocalDateTime.of(2026, 12, 12, 12, 12));

        MemberReferralJpaEntity entity = MemberReferralMapper.toEntity(original);

        assertThat(entity.getReferrerId()).isEqualTo(132L);
        assertThat(entity.getRefereeId()).isEqualTo(133L);
        assertThat(entity.getStatus()).isEqualTo("REWARDED");
    }

    @Test
    @DisplayName("엔티티 → MemberReferral 변환 시 추천인·피추천인이 뒤바뀌지 않는다")
    void toDomain() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 12, 12, 12, 12);
        MemberReferralJpaEntity entity = MemberReferralJpaEntity.create(132L, 133L, "REWARDED");
        ReflectionTestUtils.setField(entity, "id", 131L);
        ReflectionTestUtils.setField(entity, "createdAt", createdAt);
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 12, 13, 0, 0));

        MemberReferral restored = MemberReferralMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(MemberReferral.reconstitute(
            131L, MemberId.of(132L), MemberId.of(133L), MemberReferralStatus.REWARDED, createdAt));
    }
}
