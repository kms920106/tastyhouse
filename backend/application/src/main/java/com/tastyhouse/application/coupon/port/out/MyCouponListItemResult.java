package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;

public record MyCouponListItemResult(
    Long id,
    Long couponId,
    String name,
    String description,
    String discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    LocalDateTime expiredAt,
    boolean used,
    LocalDateTime usedAt,
    Long daysRemaining,
    boolean expired
) {
}
