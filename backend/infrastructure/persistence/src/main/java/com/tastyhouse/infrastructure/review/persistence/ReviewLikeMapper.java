package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewLikeState;

final class ReviewLikeMapper {
    private ReviewLikeMapper() {
    }

    static ReviewLikeState toState(ReviewLikeJpaEntity entity) {
        return new ReviewLikeState(
            entity.getId(),
            entity.getReviewId(),
            entity.getMemberId()
        );
    }

    static ReviewLikeJpaEntity toEntity(ReviewLikeState state) {
        return ReviewLikeJpaEntity.create(
            state.reviewId(),
            state.memberId()
        );
    }
}
