package com.tastyhouse.application.coupon.port.out.write;

import java.time.LocalDateTime;

public record MemberCouponState(
    Long id,
    Long memberId,
    Long couponId,
    boolean used,
    LocalDateTime usedAt,
    LocalDateTime expiredAt
) {
}
