package com.tastyhouse.core.domain.review.application.dto.command;

public record ToggleReviewLikeCommand(
    Long reviewId,
    Long memberId
) {
}
