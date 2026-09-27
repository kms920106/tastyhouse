package com.tastyhouse.infrastructure.review.persistence;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestState;

final class ReviewBlindRequestMapper {
    private ReviewBlindRequestMapper() {
    }

    static ReviewBlindRequestState toState(ReviewBlindRequestJpaEntity entity) {
        return new ReviewBlindRequestState(
            entity.getId(),
            entity.getReviewId(),
            entity.getShopId(),
            entity.getCeoId(),
            entity.getReason(),
            entity.getDetailReason(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getBlindUntil(),
            entity.getCreatedAt()
        );
    }

    static ReviewBlindRequestJpaEntity toEntity(ReviewBlindRequestState state) {
        return ReviewBlindRequestJpaEntity.create(
            state.reviewId(),
            state.shopId(),
            state.ceoId(),
            state.reason(),
            state.detailReason(),
            state.status(),
            state.rejectReason(),
            state.blindUntil()
        );
    }

    static void applyChanges(ReviewBlindRequestJpaEntity entity, ReviewBlindRequestState state) {
        entity.applyChanges(
            state.status(),
            state.rejectReason(),
            state.blindUntil()
        );
    }
}
