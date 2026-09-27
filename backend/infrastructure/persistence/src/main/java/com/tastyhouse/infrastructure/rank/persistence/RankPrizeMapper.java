package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.application.rank.port.out.write.RankPrizeState;

final class RankPrizeMapper {
    private RankPrizeMapper() {
    }

    static RankPrizeState toState(RankPrizeJpaEntity entity) {
        return new RankPrizeState(
            entity.getId(),
            entity.getRankId(),
            entity.getPrizeRank(),
            entity.getName(),
            entity.getBrand(),
            entity.getImageFileId(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static RankPrizeJpaEntity toEntity(RankPrizeState state) {
        return RankPrizeJpaEntity.create(
            state.rankId(),
            state.prizeRank(),
            state.name(),
            state.brand(),
            state.imageFileId(),
            state.deleted()
        );
    }

    static void applyChanges(RankPrizeJpaEntity entity, RankPrizeState state) {
        entity.applyChanges(
            state.prizeRank(),
            state.name(),
            state.brand(),
            state.imageFileId(),
            state.deleted()
        );
    }
}
