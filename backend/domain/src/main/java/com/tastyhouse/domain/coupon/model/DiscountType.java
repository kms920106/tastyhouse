package com.tastyhouse.domain.coupon.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.COUPON_DISCOUNT_TYPE_UNKNOWN,
                DomainErrorCode.COUPON_DISCOUNT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }
}
