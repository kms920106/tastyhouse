package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MyCouponListItemResponse(
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
    Long daysRemaining,
    Boolean isExpired
) {
    public static MyCouponListItemResponse of(
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
        LocalDateTime now = LocalDateTime.now();

        Long daysRemaining = null;
        if (!isUsed && expiredAt != null && now.isBefore(expiredAt)) {
            daysRemaining = ChronoUnit.DAYS.between(now, expiredAt);
        }

        boolean isExpired = expiredAt != null && now.isAfter(expiredAt);

        return new MyCouponListItemResponse(
            id,
            couponId,
            name, description,
            discountType,
            discountAmount,
            maxDiscountAmount,
            minOrderAmount,
            useStartAt,
            useEndAt,
            expiredAt,
            isUsed,
            usedAt,
            daysRemaining,
            isExpired
        );
    }
}
