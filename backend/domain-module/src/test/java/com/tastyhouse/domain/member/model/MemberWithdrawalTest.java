package com.tastyhouse.domain.member.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberWithdrawalTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)다")
    void of_createsTransientWithdrawal() {
        MemberId memberId = MemberId.of(1L);

        MemberWithdrawal withdrawal = MemberWithdrawal.of(
            memberId, MemberWithdrawalReason.LOW_USAGE_FREQUENCY, "자세한 사유"
        );

        assertThat(withdrawal.getId()).isNull();
        assertThat(withdrawal.getMemberId()).isEqualTo(memberId);
        assertThat(withdrawal.getReason()).isEqualTo(MemberWithdrawalReason.LOW_USAGE_FREQUENCY);
        assertThat(withdrawal.getReasonDetail()).isEqualTo("자세한 사유");
        assertThat(withdrawal.getCreatedAt()).isNull();
        assertThat(withdrawal.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        MemberWithdrawal withdrawal = MemberWithdrawal.reconstitute(
            5L, MemberId.of(1L), MemberWithdrawalReason.OTHER, "기타 사유", createdAt, updatedAt
        );

        assertThat(withdrawal.getId()).isEqualTo(5L);
        assertThat(withdrawal.getCreatedAt()).isEqualTo(createdAt);
        assertThat(withdrawal.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
