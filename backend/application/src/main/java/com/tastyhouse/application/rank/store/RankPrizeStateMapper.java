package com.tastyhouse.application.rank.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.out.write.RankPrizeState;

final class RankPrizeStateMapper {
    private RankPrizeStateMapper() {
    }

    static RankPrize toDomain(RankPrizeState state) {
        return RankPrize.reconstitute(
            state.id(),
            state.rankId() == null ? null : RankPeriodId.of(state.rankId()),
            state.prizeRank(),
            state.name(),
            state.brand(),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static RankPrizeState toState(RankPrize rankPrize) {
        return new RankPrizeState(
            rankPrize.getId(),
            rankPrize.getRankId() == null ? null : rankPrize.getRankId().value(),
            rankPrize.getPrizeRank(),
            rankPrize.getName(),
            rankPrize.getBrand(),
            rankPrize.getImageFileId() == null ? null : rankPrize.getImageFileId().value(),
            rankPrize.isDeleted(),
            rankPrize.getCreatedAt(),
            rankPrize.getUpdatedAt()
        );
    }
}
