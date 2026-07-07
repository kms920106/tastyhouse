package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public record ReviewCommentCreateCommand(
    ReviewId reviewId,
    Long memberId,
    String content
) {

    public static ReviewCommentCreateCommand of(ReviewId reviewId, Long memberId, String content) {
        return new ReviewCommentCreateCommand(reviewId, memberId, content);
    }
}
