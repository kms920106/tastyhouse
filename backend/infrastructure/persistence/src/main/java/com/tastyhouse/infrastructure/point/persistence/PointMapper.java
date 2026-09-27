package com.tastyhouse.infrastructure.point.persistence;

import com.tastyhouse.application.point.port.out.write.PointState;

final class PointMapper {
    private PointMapper() {
    }

    static PointState toState(PointJpaEntity entity) {
        return new PointState(
            entity.getId(),
            entity.getMemberId(),
            entity.getAvailablePoints(),
            entity.getExpiredThisMonth()
        );
    }

    static PointJpaEntity toEntity(PointState state) {
        return PointJpaEntity.create(
            state.memberId(),
            state.availablePoints(),
            state.expiredThisMonth()
        );
    }

    static void applyChanges(PointJpaEntity entity, PointState state) {
        entity.applyChanges(
            state.availablePoints(),
            state.expiredThisMonth()
        );
    }
}
