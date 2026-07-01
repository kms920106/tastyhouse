package com.tastyhouse.core.domain.coupon.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.model.CouponId;
import com.tastyhouse.core.domain.coupon.domain.model.MemberCouponId;

public record MemberCouponUsedEvent(
    MemberCouponId memberCouponId,
    Long memberId,
    CouponId couponId,
    LocalDateTime usedAt
) {
}
