package com.tastyhouse.application.rank.store;

import com.tastyhouse.application.rank.port.out.write.MemberReviewRankState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;

final class MemberReviewRankStateMapper {
    private MemberReviewRankStateMapper() {
    }

    static MemberReviewRank toDomain(MemberReviewRankState state) {
        return MemberReviewRank.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.reviewCount(),
            state.rankNo(),
            state.rankType() == null ? null : RankType.valueOf(state.rankType()),
            state.baseDate(),
            state.lastReviewAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static MemberReviewRankState toState(MemberReviewRank rank) {
        return new MemberReviewRankState(
            rank.getId(),
            rank.getMemberId() == null ? null : rank.getMemberId().value(),
            rank.getReviewCount(),
            rank.getRankNo(),
            rank.getRankType() == null ? null : rank.getRankType().name(),
            rank.getBaseDate(),
            rank.getLastReviewAt(),
            rank.getCreatedAt(),
            rank.getUpdatedAt()
        );
    }
}
