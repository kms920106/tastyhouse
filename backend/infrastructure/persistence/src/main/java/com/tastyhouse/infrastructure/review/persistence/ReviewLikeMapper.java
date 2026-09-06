package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReviewLikeMapper {
    private ReviewLikeMapper() {
    }

    static ReviewLike toDomain(ReviewLikeJpaEntity entity) {
        return ReviewLike.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getReviewId(), ReviewId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of)
        );
    }

    static ReviewLikeJpaEntity toEntity(ReviewLike domain) {
        return ReviewLikeJpaEntity.create(
            IdMapping.raw(domain.getReviewId(), ReviewId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value)
        );
    }
}
