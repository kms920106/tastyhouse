package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.rank.model.MemberReviewRank;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class MemberReviewRankMapper {
    private MemberReviewRankMapper() {
    }

    static MemberReviewRank toDomain(MemberReviewRankJpaEntity entity) {
        return MemberReviewRank.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getReviewCount(),
            entity.getRankNo(),
            entity.getRankType(),
            entity.getBaseDate(),
            entity.getLastReviewAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static MemberReviewRankJpaEntity toEntity(MemberReviewRank domain) {
        return MemberReviewRankJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getReviewCount(),
            domain.getRankNo(),
            domain.getRankType(),
            domain.getBaseDate(),
            domain.getLastReviewAt()
        );
    }
}
