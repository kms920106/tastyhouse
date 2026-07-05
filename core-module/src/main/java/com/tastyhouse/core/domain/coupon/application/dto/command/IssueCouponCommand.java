package com.tastyhouse.core.domain.coupon.application.dto.command;

import java.time.LocalDateTime;

public record IssueCouponCommand(
    Long memberId,
    Long couponId,
    LocalDateTime expiredAt
) {

    public static IssueCouponCommand of(Long memberId, Long couponId, LocalDateTime expiredAt) {
        return new IssueCouponCommand(memberId, couponId, expiredAt);
    }
}
