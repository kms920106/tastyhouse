package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ToggleReviewLikeCommand(
    ReviewId reviewId,
    Long memberId
) {

    public static ToggleReviewLikeCommand of(ReviewId reviewId, Long memberId) {
        return new ToggleReviewLikeCommand(reviewId, memberId);
    }
}
