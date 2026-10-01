package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPeriodId;

final class RankPrizeMapper {

    private RankPrizeMapper() {
    }

    static RankPrize toDomain(RankPrizeJpaEntity entity) {
        return RankPrize.reconstitute(
            entity.getId(),
            entity.getRankId() == null ? null : RankPeriodId.of(entity.getRankId()),
            entity.getPrizeRank(),
            entity.getName(),
            entity.getBrand(),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static RankPrizeJpaEntity toEntity(RankPrize rankPrize) {
        return RankPrizeJpaEntity.create(
            rankPrize.getRankId() == null ? null : rankPrize.getRankId().value(),
            rankPrize.getPrizeRank(),
            rankPrize.getName(),
            rankPrize.getBrand(),
            rankPrize.getImageFileId() == null ? null : rankPrize.getImageFileId().value(),
            rankPrize.isDeleted()
        );
    }

    static void applyChanges(RankPrizeJpaEntity entity, RankPrize rankPrize) {
        entity.applyChanges(
            rankPrize.getPrizeRank(),
            rankPrize.getName(),
            rankPrize.getBrand(),
            rankPrize.getImageFileId() == null ? null : rankPrize.getImageFileId().value(),
            rankPrize.isDeleted()
        );
    }
}
