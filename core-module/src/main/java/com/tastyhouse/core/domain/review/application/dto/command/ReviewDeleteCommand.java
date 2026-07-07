package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewDeleteCommand(
    ReviewId reviewId,
    Long memberId,
    Long productId
) {

    public static ReviewDeleteCommand of(ReviewId reviewId, Long memberId, Long productId) {
        return new ReviewDeleteCommand(reviewId, memberId, productId);
    }
}
