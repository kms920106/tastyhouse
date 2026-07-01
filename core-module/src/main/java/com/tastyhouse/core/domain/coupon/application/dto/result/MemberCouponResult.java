package com.tastyhouse.core.domain.coupon.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

public record MemberCouponResult(
    Long id,
    Long couponId,
    String name,
    String description,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    LocalDateTime expiredAt,
    boolean used,
    LocalDateTime usedAt
) {
    @QueryProjection
    public MemberCouponResult {
    }
}
