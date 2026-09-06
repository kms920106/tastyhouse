package com.tastyhouse.infrastructure.rank.persistence;

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

    static RankPeriodJpaEntity toEntity(RankPeriod domain) {
        return RankPeriodJpaEntity.create(
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    static void applyChanges(RankPeriodJpaEntity entity, RankPeriod domain) {
        entity.applyChanges(
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
