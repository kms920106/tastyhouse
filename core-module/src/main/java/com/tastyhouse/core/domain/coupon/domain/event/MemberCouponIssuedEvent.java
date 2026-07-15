package com.tastyhouse.core.domain.coupon.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record MemberCouponIssuedEvent(
    MemberCouponId memberCouponId,
    MemberId memberId,
    CouponId couponId,
    LocalDateTime issuedAt
) {
}
