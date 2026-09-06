package com.tastyhouse.application.coupon.port.out;

import com.tastyhouse.domain.coupon.model.DiscountType;

public record CouponSearchCondition(
    String name,
    DiscountType discountType,
    Boolean visible
) {

    public static CouponSearchCondition of(String name, DiscountType discountType, Boolean visible) {
        return new CouponSearchCondition(name, discountType, visible);
    }
}
