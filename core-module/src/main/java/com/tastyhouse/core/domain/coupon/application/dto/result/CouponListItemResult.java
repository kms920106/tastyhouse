package com.tastyhouse.core.domain.coupon.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

public record CouponListItemResult(
    Long id,
    String name,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    Integer maxDiscountCount,
    LocalDateTime issueStartAt,
    LocalDateTime issueEndAt,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    boolean visible
) {
    @QueryProjection
    public CouponListItemResult {
    }
}
