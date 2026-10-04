package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CouponDeleteCommand(Long couponId) {

    public CouponDeleteCommand {
        if (couponId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static CouponDeleteCommand of(Long couponId) {
        return new CouponDeleteCommand(couponId);
    }
}
