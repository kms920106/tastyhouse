package com.tastyhouse.application.point.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;

import static org.assertj.core.api.Assertions.assertThat;

class PointStateMapperTest {

    @Test
    @DisplayName("Point → PointState → Point 왕복 시 모든 필드가 보존된다")
    void pointRoundTrip() {
        Point original = Point.reconstitute(11L, MemberId.of(12L), 3000, 150);

        Point restored = PointStateMapper.toDomain(PointStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("PointHistory → PointHistoryState → PointHistory 왕복 시 모든 필드가 보존된다")
    void pointHistoryRoundTrip() {
        PointHistory original = PointHistory.reconstitute(
            21L, MemberId.of(22L), PointType.REFUND, 700, "주문 취소 환불",
            LocalDateTime.of(2026, 3, 4, 5, 6, 7));

        PointHistory restored = PointHistoryStateMapper.toDomain(PointHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
