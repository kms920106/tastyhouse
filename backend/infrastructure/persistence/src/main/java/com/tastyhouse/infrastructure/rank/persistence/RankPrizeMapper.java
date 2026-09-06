package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class RankPrizeMapper {
    private RankPrizeMapper() {
    }

    static RankPrize toDomain(RankPrizeJpaEntity entity) {
        return RankPrize.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getRankId(), RankPeriodId::of),
            entity.getPrizeRank(),
            entity.getName(),
            entity.getBrand(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static RankPrizeJpaEntity toEntity(RankPrize domain) {
        return RankPrizeJpaEntity.create(
            IdMapping.raw(domain.getRankId(), RankPeriodId::value),
            domain.getPrizeRank(),
            domain.getName(),
            domain.getBrand(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.isDeleted()
        );
    }

    static void applyChanges(RankPrizeJpaEntity entity, RankPrize domain) {
        entity.applyChanges(
            domain.getPrizeRank(),
            domain.getName(),
            domain.getBrand(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.isDeleted()
        );
    }
}
