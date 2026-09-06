package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ProductOptionType {
    NORMAL,

    COMMON;

    public static ProductOptionType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_OPTION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
