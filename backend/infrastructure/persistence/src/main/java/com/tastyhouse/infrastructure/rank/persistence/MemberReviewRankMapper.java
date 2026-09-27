package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.domain.rank.model.RankType;

final class MemberReviewRankMapper {
    private MemberReviewRankMapper() {
    }

    static MemberReviewRank toDomain(MemberReviewRankJpaEntity entity) {
        return MemberReviewRank.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getReviewCount(),
            entity.getRankNo(),
            entity.getRankType() == null ? null : RankType.valueOf(entity.getRankType()),
            entity.getBaseDate(),
            entity.getLastReviewAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberReviewRankJpaEntity toEntity(MemberReviewRank rank) {
        return MemberReviewRankJpaEntity.create(
            rank.getMemberId() == null ? null : rank.getMemberId().value(),
            rank.getReviewCount(),
            rank.getRankNo(),
            rank.getRankType() == null ? null : rank.getRankType().name(),
            rank.getBaseDate(),
            rank.getLastReviewAt()
        );
    }
}
