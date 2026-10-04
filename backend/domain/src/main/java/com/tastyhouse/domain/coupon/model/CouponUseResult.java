package com.tastyhouse.domain.coupon.model;

public record CouponUseResult(
    Long memberCouponId,
    int couponDiscountAmount
) {

    public static CouponUseResult of(Long memberCouponId, int couponDiscountAmount) {
        return new CouponUseResult(memberCouponId, couponDiscountAmount);
    }
}
