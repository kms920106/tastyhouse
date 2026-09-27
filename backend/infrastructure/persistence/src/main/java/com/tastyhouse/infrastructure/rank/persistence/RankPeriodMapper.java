package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.application.rank.port.out.write.RankPeriodState;

final class RankPeriodMapper {
    private RankPeriodMapper() {
    }

    static RankPeriodState toState(RankPeriodJpaEntity entity) {
        return new RankPeriodState(
            entity.getId(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static RankPeriodJpaEntity toEntity(RankPeriodState state) {
        return RankPeriodJpaEntity.create(
            state.startAt(),
            state.endAt(),
            state.visible(),
            state.deleted()
        );
    }

    static void applyChanges(RankPeriodJpaEntity entity, RankPeriodState state) {
        entity.applyChanges(
            state.startAt(),
            state.endAt(),
            state.visible(),
            state.deleted()
        );
    }
}
