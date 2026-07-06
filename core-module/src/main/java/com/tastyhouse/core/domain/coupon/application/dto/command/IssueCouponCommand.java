package com.tastyhouse.core.domain.coupon.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;

public record IssueCouponCommand(
    Long memberId,
    CouponId couponId,
    LocalDateTime expiredAt
) {

    public static IssueCouponCommand of(Long memberId, Long couponId, LocalDateTime expiredAt) {
        return new IssueCouponCommand(memberId, CouponId.of(couponId), expiredAt);
    }
}
