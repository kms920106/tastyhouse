package com.tastyhouse.core.domain.coupon.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum DiscountType {
    AMOUNT("정액 할인"),
    RATE("정률 할인");

    private final String description;

    public static DiscountType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.COUPON_DISCOUNT_TYPE_UNKNOWN,
                ErrorCode.COUPON_DISCOUNT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
