package com.tastyhouse.infrastructure.point.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class PointMapper {
    private PointMapper() {
    }

    static Point toDomain(PointJpaEntity entity) {
        return Point.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getAvailablePoints(),
            entity.getExpiredThisMonth()
        );
    }

    static PointJpaEntity toEntity(Point domain) {
        return PointJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getAvailablePoints(),
            domain.getExpiredThisMonth()
        );
    }

    static void applyChanges(PointJpaEntity entity, Point domain) {
        entity.applyChanges(
            domain.getAvailablePoints(),
            domain.getExpiredThisMonth()
        );
    }
}
