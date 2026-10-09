package com.tastyhouse.infrastructure.jpa.rank.persistence;

import com.tastyhouse.domain.rank.model.RankPeriod;

final class RankPeriodMapper {

    private RankPeriodMapper() {
    }

    static RankPeriod toDomain(RankPeriodJpaEntity entity) {
        return RankPeriod.reconstitute(
            entity.getId(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static RankPeriodJpaEntity toEntity(RankPeriod rankPeriod) {
        return RankPeriodJpaEntity.create(
            rankPeriod.getStartAt(),
            rankPeriod.getEndAt(),
            rankPeriod.isVisible(),
            rankPeriod.isDeleted()
        );
    }

    static void applyChanges(RankPeriodJpaEntity entity, RankPeriod rankPeriod) {
        entity.applyChanges(
            rankPeriod.getStartAt(),
            rankPeriod.getEndAt(),
            rankPeriod.isVisible(),
            rankPeriod.isDeleted()
        );
    }
}
