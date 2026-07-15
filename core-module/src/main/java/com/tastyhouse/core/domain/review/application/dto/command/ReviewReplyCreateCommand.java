package com.tastyhouse.core.domain.review.application.dto.command;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;

public record ReviewReplyCreateCommand(
    ReviewCommentId commentId,
    MemberId memberId,
    MemberId replyToMemberId,
    String content
) {

    public static ReviewReplyCreateCommand of(
        ReviewCommentId commentId,
        MemberId memberId,
        MemberId replyToMemberId,
        String content
    ) {
        return new ReviewReplyCreateCommand(commentId, memberId, replyToMemberId, content);
    }
}
