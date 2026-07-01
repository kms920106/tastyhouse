package com.tastyhouse.core.domain.rank.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record MemberReviewCountResult(
    Long memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
    @QueryProjection
    public MemberReviewCountResult {
    }
}
