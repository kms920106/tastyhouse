package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.vo.MemberId;

public record ReviewListItemResult(
    Long id,
    Long shopId,
    Long productId,
    MemberId memberId,
    String memberNickname,
    Double totalRating,
    String content,
    boolean hidden,
    LocalDateTime createdAt
) {

    @QueryProjection
    public ReviewListItemResult {
    }
}
