package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record CreateReviewCommentCommand(
    ReviewId reviewId,
    Long memberId,
    String content
) {

    public static CreateReviewCommentCommand of(ReviewId reviewId, Long memberId, String content) {
        return new CreateReviewCommentCommand(reviewId, memberId, content);
    }
}
