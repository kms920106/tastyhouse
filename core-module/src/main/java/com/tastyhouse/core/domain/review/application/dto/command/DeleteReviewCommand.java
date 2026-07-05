package com.tastyhouse.core.domain.review.application.dto.command;

public record DeleteReviewCommand(
    Long reviewId,
    Long memberId,
    Long productId
) {

    public static DeleteReviewCommand of(Long reviewId, Long memberId, Long productId) {
        return new DeleteReviewCommand(reviewId, memberId, productId);
    }
}
