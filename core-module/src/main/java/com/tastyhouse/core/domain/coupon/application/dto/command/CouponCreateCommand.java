package com.tastyhouse.core.domain.coupon.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;

public record CouponCreateCommand(
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

    public static CouponCreateCommand of(
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
        return new CouponCreateCommand(
            name, description, discountType, discountAmount, maxDiscountAmount,
            minOrderAmount, maxDiscountCount, issueStartAt, issueEndAt,
            useStartAt, useEndAt, visible
        );
    }
}
