package com.tastyhouse.infrastructure.jpa.point.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;

final class PointMapper {

    private PointMapper() {
    }

    static Point toDomain(PointJpaEntity entity) {
        return Point.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getAvailablePoints(),
            entity.getExpiredThisMonth()
        );
    }

    static PointJpaEntity toEntity(Point point) {
        return PointJpaEntity.create(
            point.getMemberId() == null ? null : point.getMemberId().value(),
            point.getAvailablePoints(),
            point.getExpiredThisMonth()
        );
    }

    static void applyChanges(PointJpaEntity entity, Point point) {
        entity.applyChanges(
            point.getAvailablePoints(),
            point.getExpiredThisMonth()
        );
    }
}
