package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestState;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReviewBlindRequestStateMapper {
    private ReviewBlindRequestStateMapper() {
    }

    static ReviewBlindRequest toDomain(ReviewBlindRequestState state) {
        return ReviewBlindRequest.reconstitute(
            state.id(),
            state.reviewId() == null ? null : ReviewId.of(state.reviewId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.ceoId() == null ? null : CeoId.of(state.ceoId()),
            state.reason() == null ? null : ReviewBlindReason.valueOf(state.reason()),
            state.detailReason(),
            state.status() == null ? null : ReviewBlindStatus.valueOf(state.status()),
            state.rejectReason(),
            state.blindUntil(),
            state.createdAt()
        );
    }

    static ReviewBlindRequestState toState(ReviewBlindRequest request) {
        return new ReviewBlindRequestState(
            request.getId(),
            request.getReviewId() == null ? null : request.getReviewId().value(),
            request.getShopId() == null ? null : request.getShopId().value(),
            request.getCeoId() == null ? null : request.getCeoId().value(),
            request.getReason() == null ? null : request.getReason().name(),
            request.getDetailReason(),
            request.getStatus() == null ? null : request.getStatus().name(),
            request.getRejectReason(),
            request.getBlindUntil(),
            request.getCreatedAt()
        );
    }
}
