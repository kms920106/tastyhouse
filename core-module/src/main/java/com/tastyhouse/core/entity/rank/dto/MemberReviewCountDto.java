package com.tastyhouse.core.entity.rank.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record MemberReviewCountDto(
    Long memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
    @QueryProjection
    public MemberReviewCountDto {
    }
}
