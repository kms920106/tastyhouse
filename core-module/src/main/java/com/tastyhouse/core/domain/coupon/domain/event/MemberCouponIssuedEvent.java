package com.tastyhouse.core.domain.coupon.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;

public record MemberCouponIssuedEvent(
    MemberCouponId memberCouponId,
    Long memberId,
    CouponId couponId,
    LocalDateTime issuedAt
) {
}
