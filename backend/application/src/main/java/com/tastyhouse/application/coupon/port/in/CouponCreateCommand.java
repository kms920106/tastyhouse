package com.tastyhouse.application.coupon.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CouponCreateCommand(
    String name,
    String description,
    String discountType,
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
    public CouponCreateCommand {
        if (name == null || discountType == null || discountAmount == null
            || issueStartAt == null || issueEndAt == null || useStartAt == null || useEndAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
