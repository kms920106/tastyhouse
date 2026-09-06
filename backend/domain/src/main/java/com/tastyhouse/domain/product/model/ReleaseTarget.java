package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ReleaseTarget {
    SOLD_OUT,

    HIDDEN,

    ALL;

    public static ReleaseTarget from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PRODUCT_RELEASE_TARGET_UNKNOWN,
                ErrorCode.PRODUCT_RELEASE_TARGET_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
