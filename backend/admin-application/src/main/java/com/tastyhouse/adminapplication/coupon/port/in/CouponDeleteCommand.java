package com.tastyhouse.adminapplication.coupon.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 쿠폰 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
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
