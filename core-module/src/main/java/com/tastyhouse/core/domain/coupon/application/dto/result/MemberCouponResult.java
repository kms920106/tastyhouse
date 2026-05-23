package com.tastyhouse.core.domain.coupon.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

import java.time.LocalDateTime;

public record MemberCouponResult(
    Long id,
    Long couponId,
    String name,
    String description,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    LocalDateTime expiredAt,
    Boolean isUsed,
    LocalDateTime usedAt
) {
    @QueryProjection
    public MemberCouponResult {
    }
}
