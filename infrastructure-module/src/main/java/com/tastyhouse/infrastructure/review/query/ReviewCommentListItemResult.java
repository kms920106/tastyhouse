package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;

public record ReviewCommentListItemResult(
    Long id,
    MemberId memberId,
    String memberNickname,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {

    public static ReviewCommentListItemResult of(
        Long id,
        MemberId memberId,
        String memberNickname,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ReviewCommentListItemResult(
            id,
            memberId,
            memberNickname,
            content,
            hidden,
            createdAt
        );
    }
}
