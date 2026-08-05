package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

public record ReviewReplyListItemResult(
    Long id,
    Long commentId,
    Long memberId,
    String memberNickname,
    Long replyToMemberId,
    String replyToMemberNickname,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {

    public static ReviewReplyListItemResult of(
        Long id,
        Long commentId,
        Long memberId,
        String memberNickname,
        Long replyToMemberId,
        String replyToMemberNickname,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ReviewReplyListItemResult(
            id,
            commentId,
            memberId,
            memberNickname,
            replyToMemberId,
            replyToMemberNickname,
            content,
            hidden,
            createdAt
        );
    }
}
