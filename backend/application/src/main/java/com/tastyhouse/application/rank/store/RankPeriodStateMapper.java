package com.tastyhouse.application.rank.store;

import com.tastyhouse.application.rank.port.out.write.RankPeriodState;
import com.tastyhouse.domain.rank.model.RankPeriod;

final class RankPeriodStateMapper {
    private RankPeriodStateMapper() {
    }

    static RankPeriod toDomain(RankPeriodState state) {
        return RankPeriod.reconstitute(
            state.id(),
            state.startAt(),
            state.endAt(),
            state.visible(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static RankPeriodState toState(RankPeriod rankPeriod) {
        return new RankPeriodState(
            rankPeriod.getId(),
            rankPeriod.getStartAt(),
            rankPeriod.getEndAt(),
            rankPeriod.isVisible(),
            rankPeriod.isDeleted(),
            rankPeriod.getCreatedAt(),
            rankPeriod.getUpdatedAt()
        );
    }
}
