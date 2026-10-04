package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ProductOptionType {

    NORMAL,

    COMMON;

    public static ProductOptionType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainException(DomainErrorCode.PRODUCT_OPTION_TYPE_UNKNOWN,
                DomainErrorCode.PRODUCT_OPTION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
