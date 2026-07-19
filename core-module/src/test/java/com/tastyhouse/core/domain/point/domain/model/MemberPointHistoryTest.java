package com.tastyhouse.core.domain.point.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberPointHistoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 필드가 세팅된다")
    void of_createsTransientMemberPointHistory() {
        MemberPointHistory history = MemberPointHistory.of(MemberId.of(1L), PointType.EARNED, 1000, "가입 축하 적립");

        assertThat(history.getId()).isNull();
        assertThat(history.getMemberId()).isEqualTo(MemberId.of(1L));
        assertThat(history.getPointType()).isEqualTo(PointType.EARNED);
        assertThat(history.getPointAmount()).isEqualTo(1000);
        assertThat(history.getReason()).isEqualTo("가입 축하 적립");
        assertThat(history.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        MemberPointHistory history = MemberPointHistory.reconstitute(
            1L, MemberId.of(2L), PointType.USE, -500, "주문 결제 사용", createdAt
        );

        assertThat(history.getId()).isEqualTo(1L);
        assertThat(history.getMemberId()).isEqualTo(MemberId.of(2L));
        assertThat(history.getPointType()).isEqualTo(PointType.USE);
        assertThat(history.getPointAmount()).isEqualTo(-500);
        assertThat(history.getReason()).isEqualTo("주문 결제 사용");
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }
}
