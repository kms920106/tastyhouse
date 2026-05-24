package com.tastyhouse.core.domain.rank.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record MemberReviewCountResult(
    Long memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
    @QueryProjection
    public MemberReviewCountResult {
    }
}
