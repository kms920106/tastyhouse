package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record CouponIssueCommand(
    Long couponId,
    Long memberId
) {
    public CouponIssueCommand {
        if (couponId == null || memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
