package com.tastyhouse.core.domain.review.application.dto.command;

public record CreateReviewCommentCommand(
    Long reviewId,
    Long memberId,
    String content
) {

    public static CreateReviewCommentCommand of(Long reviewId, Long memberId, String content) {
        return new CreateReviewCommentCommand(reviewId, memberId, content);
    }
}
