package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyState;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReviewOwnerReplyStateMapper {
    private ReviewOwnerReplyStateMapper() {
    }

    static ReviewOwnerReply toDomain(ReviewOwnerReplyState state) {
        return ReviewOwnerReply.reconstitute(
            state.id(),
            state.reviewId() == null ? null : ReviewId.of(state.reviewId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.ceoId() == null ? null : CeoId.of(state.ceoId()),
            state.content(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ReviewOwnerReplyState toState(ReviewOwnerReply reply) {
        return new ReviewOwnerReplyState(
            reply.getId(),
            reply.getReviewId() == null ? null : reply.getReviewId().value(),
            reply.getShopId() == null ? null : reply.getShopId().value(),
            reply.getCeoId() == null ? null : reply.getCeoId().value(),
            reply.getContent(),
            reply.getCreatedAt(),
            reply.getUpdatedAt()
        );
    }
}
