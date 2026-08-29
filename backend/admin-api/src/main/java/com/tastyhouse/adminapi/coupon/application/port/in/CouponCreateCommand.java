package com.tastyhouse.adminapi.coupon.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 쿠폰 등록 command.
 *
 * <p>형식·범위 검증은 Request의 jakarta.validation이 담당하고, 이 record는 구조적 가드만 둔다.
 * {@code maxDiscountAmount}·{@code minOrderAmount}·{@code maxDiscountCount}는 미지정 허용값이라 null을 받는다.
 *
 * <p>{@code Integer} 금액 4개와 {@code LocalDateTime} 4개가 연달아 있어 순서가 뒤바뀌어도 컴파일된다.
 * 조립은 반드시 이름 있는 접근자로 한다.
 */
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
