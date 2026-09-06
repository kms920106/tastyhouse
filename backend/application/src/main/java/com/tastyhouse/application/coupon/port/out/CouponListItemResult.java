package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.model.DiscountType;

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
}
