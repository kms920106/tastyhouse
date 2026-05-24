package com.tastyhouse.core.domain.review.application.dto.command;

public record DeleteReviewCommand(
    Long reviewId,
    Long memberId,
    Long productId
) {
}
