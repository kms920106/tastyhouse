package com.tastyhouse.core.domain.review.application.dto.command;

public record CreateReviewCommentCommand(
    Long reviewId,
    Long memberId,
    String content
) {
}
