package com.tastyhouse.application.coupon.port.out.write;

import java.time.LocalDateTime;

public record CouponState(
    Long id,
    String name,
    String description,
    String discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    Integer maxDiscountCount,
    LocalDateTime issueStartAt,
    LocalDateTime issueEndAt,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
