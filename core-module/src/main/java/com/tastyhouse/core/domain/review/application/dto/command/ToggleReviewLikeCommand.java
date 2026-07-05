package com.tastyhouse.core.domain.review.application.dto.command;

public record ToggleReviewLikeCommand(
    Long reviewId,
    Long memberId
) {

    public static ToggleReviewLikeCommand of(Long reviewId, Long memberId) {
        return new ToggleReviewLikeCommand(reviewId, memberId);
    }
}
