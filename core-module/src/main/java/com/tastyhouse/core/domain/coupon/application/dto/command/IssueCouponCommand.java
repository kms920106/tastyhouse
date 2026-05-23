package com.tastyhouse.core.domain.coupon.application.dto.command;

import java.time.LocalDateTime;

public record IssueCouponCommand(
    Long memberId,
    Long couponId,
    LocalDateTime expiredAt
) {
}
