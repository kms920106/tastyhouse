package com.tastyhouse.core.domain.coupon.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record IssueCouponCommand(
    MemberId memberId,
    CouponId couponId,
    LocalDateTime expiredAt
) {

    public static IssueCouponCommand of(MemberId memberId, Long couponId, LocalDateTime expiredAt) {
        return new IssueCouponCommand(memberId, CouponId.of(couponId), expiredAt);
    }
}
