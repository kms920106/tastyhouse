package com.tastyhouse.core.domain.coupon.application.dto.result;

public record UseCouponResult(
    Long memberCouponId,
    int couponDiscountAmount
) {
}
