package com.tastyhouse.core.domain.coupon.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

public record CouponUpdateCommand(
    String name,
    String description,
    DiscountType discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    Integer maxDiscountCount,
    LocalDateTime issueStartAt,
    LocalDateTime issueEndAt,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    boolean visible
) {

    public static CouponUpdateCommand of(
        String name,
        String description,
        DiscountType discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        return new CouponUpdateCommand(
            name, description, discountType, discountAmount, maxDiscountAmount,
            minOrderAmount, maxDiscountCount, issueStartAt, issueEndAt,
            useStartAt, useEndAt, visible
        );
    }
}
