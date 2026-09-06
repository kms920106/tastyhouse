package com.tastyhouse.domain.coupon.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;

public record MemberCouponUsedEvent(
    MemberCouponId memberCouponId,
    MemberId memberId,
    CouponId couponId,
    LocalDateTime usedAt
) {
}
