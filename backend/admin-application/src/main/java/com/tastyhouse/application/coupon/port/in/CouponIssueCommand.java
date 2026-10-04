package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record CouponIssueCommand(
    Long couponId,
    Long memberId
) {

    public CouponIssueCommand {
        if (couponId == null || memberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
