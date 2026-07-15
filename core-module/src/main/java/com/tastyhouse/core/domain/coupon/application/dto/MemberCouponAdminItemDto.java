package com.tastyhouse.core.domain.coupon.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberCouponAdminItemDto(
    Long id,
    MemberId memberId,
    boolean used,
    LocalDateTime usedAt,
    LocalDateTime expiredAt,
    LocalDateTime createdAt
) {
    @QueryProjection
    public MemberCouponAdminItemDto {
    }
}
