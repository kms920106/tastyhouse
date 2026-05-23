package com.tastyhouse.core.domain.coupon.domain.model;

public record MemberCouponId(Long value) {

    public MemberCouponId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberCouponId는 양수여야 합니다: " + value);
        }
    }
}
