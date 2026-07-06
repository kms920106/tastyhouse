package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;

public record CreateReviewReplyCommand(
    ReviewCommentId commentId,
    Long memberId,
    Long replyToMemberId,
    String content
) {

    public static CreateReviewReplyCommand of(
        ReviewCommentId commentId,
        Long memberId,
        Long replyToMemberId,
        String content
    ) {
        return new CreateReviewReplyCommand(commentId, memberId, replyToMemberId, content);
    }
}
