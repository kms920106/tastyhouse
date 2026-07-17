package com.tastyhouse.core.domain.coupon.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;

public record CouponDetailResult(
    CouponId couponId,
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
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static CouponDetailResult from(Coupon coupon) {
        return new CouponDetailResult(
            coupon.getCouponId(),
            coupon.getName(),
            coupon.getDescription(),
            coupon.getDiscountType(),
            coupon.getDiscountAmount(),
            coupon.getMaxDiscountAmount(),
            coupon.getMinOrderAmount(),
            coupon.getMaxDiscountCount(),
            coupon.getIssueStartAt(),
            coupon.getIssueEndAt(),
            coupon.getUseStartAt(),
            coupon.getUseEndAt(),
            coupon.isVisible(),
            coupon.getCreatedAt(),
            coupon.getUpdatedAt()
        );
    }
}
