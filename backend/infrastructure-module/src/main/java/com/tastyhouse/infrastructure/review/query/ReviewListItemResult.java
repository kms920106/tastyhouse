package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record ReviewListItemResult(
    Long id,
    Long shopId,
    Long productId,
    Long memberId,
    String memberNickname,
    Double totalRating,
    String content,
    boolean hidden,
    boolean ownerOnly,
    LocalDateTime createdAt
) {

    @QueryProjection
    public ReviewListItemResult {
    }
}
