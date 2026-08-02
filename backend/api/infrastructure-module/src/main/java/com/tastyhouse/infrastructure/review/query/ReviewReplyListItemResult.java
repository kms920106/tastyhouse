package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public record ReviewReplyListItemResult(
    Long id,
    Long commentId,
    MemberId memberId,
    String memberNickname,
    MemberId replyToMemberId,
    String replyToMemberNickname,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {

    public static ReviewReplyListItemResult of(
        Long id,
        Long commentId,
        MemberId memberId,
        String memberNickname,
        MemberId replyToMemberId,
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
