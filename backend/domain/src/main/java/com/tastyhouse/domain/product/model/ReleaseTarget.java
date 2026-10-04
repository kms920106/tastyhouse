package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ReleaseTarget {

    SOLD_OUT,

    HIDDEN,

    ALL;

    public static ReleaseTarget from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.PRODUCT_RELEASE_TARGET_UNKNOWN,
                DomainErrorCode.PRODUCT_RELEASE_TARGET_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
