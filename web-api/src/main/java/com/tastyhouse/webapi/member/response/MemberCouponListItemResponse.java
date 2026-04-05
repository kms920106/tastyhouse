package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.coupon.DiscountType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MemberCouponListItemResponse(
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
        LocalDateTime usedAt,
        Long daysRemaining
) {
    public static MemberCouponListItemResponse of(
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
        Long daysRemaining = null;
        if (!isUsed && expiredAt != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(expiredAt)) {
                daysRemaining = ChronoUnit.DAYS.between(now, expiredAt);
            }
        }

        return new MemberCouponListItemResponse(
            id, couponId, name, description, discountType,
            discountAmount, maxDiscountAmount, minOrderAmount,
            useStartAt, useEndAt, expiredAt, isUsed, usedAt, daysRemaining
        );
    }
}
