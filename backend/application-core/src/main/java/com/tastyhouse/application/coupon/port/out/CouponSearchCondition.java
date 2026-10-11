package com.tastyhouse.application.coupon.port.out;

public record CouponSearchCondition(
    String name,
    String discountType,
    Boolean visible
) {

    public static CouponSearchCondition of(String name, String discountType, Boolean visible) {
        return new CouponSearchCondition(name, discountType, visible);
    }
}
