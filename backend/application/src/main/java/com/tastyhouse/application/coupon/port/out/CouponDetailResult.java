package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;

public record CouponDetailResult(
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
