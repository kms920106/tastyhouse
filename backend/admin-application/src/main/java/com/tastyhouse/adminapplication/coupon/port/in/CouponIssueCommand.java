package com.tastyhouse.adminapplication.coupon.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 쿠폰 회원 발급 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code couponId}(발급할 쿠폰)와 {@code memberId}(발급 대상 회원)는 둘 다 {@code Long}이라
 * 순서가 뒤바뀌어도 컴파일된다. 조립은 반드시 이름 있는 접근자로 한다.
 */
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
