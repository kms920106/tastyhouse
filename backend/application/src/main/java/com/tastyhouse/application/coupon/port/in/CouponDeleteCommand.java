package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CouponDeleteCommand(Long couponId) {
    public CouponDeleteCommand {
        if (couponId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static CouponDeleteCommand of(Long couponId) {
        return new CouponDeleteCommand(couponId);
    }
}
