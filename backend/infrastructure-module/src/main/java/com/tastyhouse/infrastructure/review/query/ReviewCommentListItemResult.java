package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

public record ReviewCommentListItemResult(
    Long id,
    Long memberId,
    String memberNickname,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {

    public static ReviewCommentListItemResult of(
        Long id,
        Long memberId,
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
