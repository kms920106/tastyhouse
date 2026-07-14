package com.tastyhouse.core.domain.coupon.application.dto;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

public record CouponSearchCondition(
    String name,
    DiscountType discountType,
    Boolean visible
) {

    public static CouponSearchCondition of(String name, DiscountType discountType, Boolean visible) {
        return new CouponSearchCondition(name, discountType, visible);
    }
}
