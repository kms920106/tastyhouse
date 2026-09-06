package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.model.DiscountType;

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
}
