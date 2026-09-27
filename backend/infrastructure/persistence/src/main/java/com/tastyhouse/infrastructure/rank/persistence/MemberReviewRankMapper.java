package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.application.rank.port.out.write.MemberReviewRankState;

final class MemberReviewRankMapper {
    private MemberReviewRankMapper() {
    }

    static MemberReviewRankState toState(MemberReviewRankJpaEntity entity) {
        return new MemberReviewRankState(
            entity.getId(),
            entity.getMemberId(),
            entity.getReviewCount(),
            entity.getRankNo(),
            entity.getRankType(),
            entity.getBaseDate(),
            entity.getLastReviewAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberReviewRankJpaEntity toEntity(MemberReviewRankState state) {
        return MemberReviewRankJpaEntity.create(
            state.memberId(),
            state.reviewCount(),
            state.rankNo(),
            state.rankType(),
            state.baseDate(),
            state.lastReviewAt()
        );
    }
}
