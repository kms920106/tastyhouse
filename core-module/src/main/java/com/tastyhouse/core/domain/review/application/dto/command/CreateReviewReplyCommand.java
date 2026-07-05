package com.tastyhouse.core.domain.review.application.dto.command;

public record CreateReviewReplyCommand(
    Long commentId,
    Long memberId,
    Long replyToMemberId,
    String content
) {

    public static CreateReviewReplyCommand of(
        Long commentId,
        Long memberId,
        Long replyToMemberId,
        String content
    ) {
        return new CreateReviewReplyCommand(commentId, memberId, replyToMemberId, content);
    }
}
