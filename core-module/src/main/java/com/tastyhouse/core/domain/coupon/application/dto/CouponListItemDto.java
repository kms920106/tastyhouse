package com.tastyhouse.core.domain.coupon.application.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

public record CouponListItemDto(
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
    public CouponListItemDto {
    }
}
