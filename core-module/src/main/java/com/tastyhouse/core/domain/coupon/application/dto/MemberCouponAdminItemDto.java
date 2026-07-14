package com.tastyhouse.core.domain.coupon.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record MemberCouponAdminItemDto(
    Long id,
    Long memberId,
    boolean used,
    LocalDateTime usedAt,
    LocalDateTime expiredAt,
    LocalDateTime createdAt
) {
    @QueryProjection
    public MemberCouponAdminItemDto {
    }
}
