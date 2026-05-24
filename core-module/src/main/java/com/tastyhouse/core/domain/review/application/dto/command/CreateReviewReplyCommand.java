package com.tastyhouse.core.domain.review.application.dto.command;

public record CreateReviewReplyCommand(
    Long commentId,
    Long memberId,
    Long replyToMemberId,
    String content
) {
}
