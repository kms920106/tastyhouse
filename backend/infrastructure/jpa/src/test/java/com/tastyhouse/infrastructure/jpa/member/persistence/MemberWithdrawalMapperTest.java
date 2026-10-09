package com.tastyhouse.infrastructure.jpa.member.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.model.MemberWithdrawal;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberWithdrawalMapperTest {

    @Test
    @DisplayName("MemberWithdrawal → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        MemberWithdrawalJpaEntity entity = MemberWithdrawalMapper.toEntity(withdrawal());

        assertThat(entity.getMemberId()).isEqualTo(112L);
        assertThat(entity.getReason()).isEqualTo("PRIVACY_CONCERNS");
        assertThat(entity.getReasonDetail()).isEqualTo("상세 사유");
    }

    @Test
    @DisplayName("엔티티 → MemberWithdrawal 변환 시 모든 필드가 보존된다")
    void toDomain() {
        MemberWithdrawal original = withdrawal();
        MemberWithdrawalJpaEntity entity = MemberWithdrawalMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        MemberWithdrawal restored = MemberWithdrawalMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static MemberWithdrawal withdrawal() {
        return MemberWithdrawal.reconstitute(
            111L, MemberId.of(112L), MemberWithdrawalReason.PRIVACY_CONCERNS, "상세 사유",
            LocalDateTime.of(2026, 10, 10, 10, 10),
            LocalDateTime.of(2026, 11, 11, 11, 11));
    }
}
