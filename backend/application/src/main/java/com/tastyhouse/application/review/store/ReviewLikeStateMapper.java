package com.tastyhouse.application.review.store;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewLikeState;

final class ReviewLikeStateMapper {
    private ReviewLikeStateMapper() {
    }

    static ReviewLike toDomain(ReviewLikeState state) {
        return ReviewLike.reconstitute(
            state.id(),
            state.reviewId() == null ? null : ReviewId.of(state.reviewId()),
            state.memberId() == null ? null : MemberId.of(state.memberId())
        );
    }

    static ReviewLikeState toState(ReviewLike reviewLike) {
        return new ReviewLikeState(
            reviewLike.getId(),
            reviewLike.getReviewId() == null ? null : reviewLike.getReviewId().value(),
            reviewLike.getMemberId() == null ? null : reviewLike.getMemberId().value()
        );
    }
}
