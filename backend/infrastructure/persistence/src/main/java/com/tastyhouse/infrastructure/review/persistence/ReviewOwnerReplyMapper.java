package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyState;

final class ReviewOwnerReplyMapper {
    private ReviewOwnerReplyMapper() {
    }

    static ReviewOwnerReplyState toState(ReviewOwnerReplyJpaEntity entity) {
        return new ReviewOwnerReplyState(
            entity.getId(),
            entity.getReviewId(),
            entity.getShopId(),
            entity.getCeoId(),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ReviewOwnerReplyJpaEntity toEntity(ReviewOwnerReplyState state) {
        return ReviewOwnerReplyJpaEntity.create(
            state.reviewId(),
            state.shopId(),
            state.ceoId(),
            state.content()
        );
    }

    static void applyChanges(ReviewOwnerReplyJpaEntity entity, ReviewOwnerReplyState state) {
        entity.applyChanges(state.content());
    }
}
