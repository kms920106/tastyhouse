package com.tastyhouse.adminapi.coupon.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 쿠폰 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code Integer} 금액 4개와 {@code LocalDateTime} 4개가 연달아 있어 순서가 뒤바뀌어도 컴파일된다.
 * 조립은 반드시 이름 있는 접근자로 한다.
 */
public record CouponUpdateCommand(
    Long couponId,
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
    public CouponUpdateCommand {
        if (couponId == null || name == null || discountType == null || discountAmount == null
            || issueStartAt == null || issueEndAt == null || useStartAt == null || useEndAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
