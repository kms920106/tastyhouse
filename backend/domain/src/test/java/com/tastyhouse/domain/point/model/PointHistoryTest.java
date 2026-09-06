package com.tastyhouse.domain.point.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryTest {
    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 필드가 세팅된다")
    void of_createsTransientPointHistory() {
        PointHistory history = PointHistory.of(MemberId.of(1L), PointType.EARNED, 1000, "가입 축하 적립");

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

        PointHistory history = PointHistory.reconstitute(
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
