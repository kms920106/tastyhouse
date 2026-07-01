package com.tastyhouse.webapi.member.response;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

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
    boolean used,
    LocalDateTime usedAt,
    Long daysRemaining,
    boolean expired
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
        boolean used,
        LocalDateTime usedAt
    ) {
        LocalDateTime now = LocalDateTime.now();

        Long daysRemaining = null;
        if (!used && expiredAt != null && now.isBefore(expiredAt)) {
            daysRemaining = ChronoUnit.DAYS.between(now, expiredAt);
        }

        boolean expired = expiredAt != null && now.isAfter(expiredAt);

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
            used,
            usedAt,
            daysRemaining,
            expired
        );
    }
}
