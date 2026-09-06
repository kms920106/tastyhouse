package com.tastyhouse.domain.coupon.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum DiscountType {
    AMOUNT("정액 할인"),
    RATE("정률 할인");

    private final String description;

    DiscountType(String description) {
        this.description = description;
    }

    public static DiscountType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.COUPON_DISCOUNT_TYPE_UNKNOWN,
                ErrorCode.COUPON_DISCOUNT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
