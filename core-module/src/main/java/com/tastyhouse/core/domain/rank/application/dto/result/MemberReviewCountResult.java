package com.tastyhouse.core.domain.rank.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberReviewCountResult(
    MemberId memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
    @QueryProjection
    public MemberReviewCountResult {
    }
}
