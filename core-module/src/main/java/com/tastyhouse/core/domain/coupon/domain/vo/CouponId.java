package com.tastyhouse.core.domain.coupon.domain.vo;

public record CouponId(Long value) {

    public CouponId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("CouponId는 양수여야 합니다: " + value);
        }
    }

    public static CouponId of(Long value) {
        return new CouponId(value);
    }
}
