package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;

final class ReviewLikeMapper {

    private ReviewLikeMapper() {
    }

    static ReviewLike toDomain(ReviewLikeJpaEntity entity) {
        return ReviewLike.reconstitute(
            entity.getId(),
            entity.getReviewId() == null ? null : ReviewId.of(entity.getReviewId()),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId())
        );
    }

    static ReviewLikeJpaEntity toEntity(ReviewLike reviewLike) {
        return ReviewLikeJpaEntity.create(
            reviewLike.getReviewId() == null ? null : reviewLike.getReviewId().value(),
            reviewLike.getMemberId() == null ? null : reviewLike.getMemberId().value()
        );
    }
}
