package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;

public record CouponListItemResult(
    Long id,
    String name,
    String discountType,
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
