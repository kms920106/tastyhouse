package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewCommentState;

final class ReviewCommentMapper {
    private ReviewCommentMapper() {
    }

    static ReviewCommentState toState(ReviewCommentJpaEntity entity) {
        return new ReviewCommentState(
            entity.getId(),
            entity.getReviewId(),
            entity.getMemberId(),
            entity.getContent(),
            entity.isHidden(),
            entity.getCreatedAt()
        );
    }

    static ReviewCommentJpaEntity toEntity(ReviewCommentState state) {
        return ReviewCommentJpaEntity.create(
            state.reviewId(),
            state.memberId(),
            state.content(),
            state.hidden()
        );
    }

    static void applyChanges(ReviewCommentJpaEntity entity, ReviewCommentState state) {
        entity.applyChanges(state.hidden());
    }
}
