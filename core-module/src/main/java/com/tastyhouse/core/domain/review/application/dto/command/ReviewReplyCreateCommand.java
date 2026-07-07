package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;

public record ReviewReplyCreateCommand(
    ReviewCommentId commentId,
    Long memberId,
    Long replyToMemberId,
    String content
) {

    public static ReviewReplyCreateCommand of(
        ReviewCommentId commentId,
        Long memberId,
        Long replyToMemberId,
        String content
    ) {
        return new ReviewReplyCreateCommand(commentId, memberId, replyToMemberId, content);
    }
}
