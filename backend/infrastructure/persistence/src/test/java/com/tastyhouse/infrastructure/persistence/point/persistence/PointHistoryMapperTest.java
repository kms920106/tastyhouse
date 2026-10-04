package com.tastyhouse.infrastructure.persistence.point.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryMapperTest {

    @Test
    @DisplayName("PointHistory → 엔티티 변환 시 enum은 name, VO는 value로 컬럼에 채워진다")
    void toEntity() {
        PointHistory history = PointHistory.reconstitute(
            21L, MemberId.of(22L), PointType.REFUND, 700, "주문 취소 환불",
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        PointHistoryJpaEntity entity = PointHistoryMapper.toEntity(history);

        assertThat(entity.getMemberId()).isEqualTo(22L);
        assertThat(entity.getPointType()).isEqualTo("REFUND");
        assertThat(entity.getPointAmount()).isEqualTo(700);
        assertThat(entity.getReason()).isEqualTo("주문 취소 환불");
    }

    @Test
    @DisplayName("엔티티 → PointHistory 변환 시 id·생성 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        PointHistoryJpaEntity entity = PointHistoryJpaEntity.create(22L, "REFUND", 700, "주문 취소 환불");
        ReflectionTestUtils.setField(entity, "id", 21L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 3, 4, 5, 6, 7));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 3, 4, 5, 6, 8));

        PointHistory history = PointHistoryMapper.toDomain(entity);

        PointHistory expected = PointHistory.reconstitute(
            21L, MemberId.of(22L), PointType.REFUND, 700, "주문 취소 환불",
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));
        assertThat(history).usingRecursiveComparison().isEqualTo(expected);
    }
}
