package com.tastyhouse.core.domain.coupon.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberCouponItemResult(
    Long id,
    MemberId memberId,
    boolean used,
    LocalDateTime usedAt,
    LocalDateTime expiredAt,
    LocalDateTime createdAt
) {
    @QueryProjection
    public MemberCouponItemResult {
    }
}
