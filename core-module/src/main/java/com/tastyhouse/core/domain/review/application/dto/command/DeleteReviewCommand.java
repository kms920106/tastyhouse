package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record DeleteReviewCommand(
    ReviewId reviewId,
    Long memberId,
    Long productId
) {

    public static DeleteReviewCommand of(ReviewId reviewId, Long memberId, Long productId) {
        return new DeleteReviewCommand(reviewId, memberId, productId);
    }
}
