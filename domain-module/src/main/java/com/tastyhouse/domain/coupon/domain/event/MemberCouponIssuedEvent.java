package com.tastyhouse.domain.coupon.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.domain.member.domain.vo.MemberId;

public record MemberCouponIssuedEvent(
    MemberCouponId memberCouponId,
    MemberId memberId,
    CouponId couponId,
    LocalDateTime issuedAt
) {
}
