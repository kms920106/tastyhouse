package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;

public record MemberCouponItemResult(
    Long id,
    Long memberId,
    boolean used,
    LocalDateTime usedAt,
    LocalDateTime expiredAt,
    LocalDateTime createdAt
) {
}
