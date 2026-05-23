package com.tastyhouse.core.domain.coupon.domain.event;

import com.tastyhouse.core.domain.coupon.domain.model.CouponId;
import com.tastyhouse.core.domain.coupon.domain.model.MemberCouponId;

import java.time.LocalDateTime;

public record MemberCouponIssuedEvent(
    MemberCouponId memberCouponId,
    Long memberId,
    CouponId couponId,
    LocalDateTime issuedAt
) {
}
