package com.tastyhouse.infrastructure.jpa.point.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;

import static org.assertj.core.api.Assertions.assertThat;

class PointMapperTest {

    @Test
    @DisplayName("Point → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        Point point = Point.reconstitute(11L, MemberId.of(12L), 3000, 150);

        PointJpaEntity entity = PointMapper.toEntity(point);

        assertThat(entity.getMemberId()).isEqualTo(12L);
        assertThat(entity.getAvailablePoints()).isEqualTo(3000);
        assertThat(entity.getExpiredThisMonth()).isEqualTo(150);
    }

    @Test
    @DisplayName("엔티티 → Point 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomain() {
        PointJpaEntity entity = PointJpaEntity.create(12L, 3000, 150);
        ReflectionTestUtils.setField(entity, "id", 11L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 3, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 3, 2, 0, 0));

        Point point = PointMapper.toDomain(entity);

        assertThat(point).usingRecursiveComparison()
            .isEqualTo(Point.reconstitute(11L, MemberId.of(12L), 3000, 150));
    }
}
