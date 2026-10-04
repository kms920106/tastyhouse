package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ProductFeedbackType {

    PRICE,

    IMAGE,

    COMPOSITION,

    SOLD_OUT,

    ETC;

    public static ProductFeedbackType from(String value) {
        if (value == null) {
            throw new DomainException(DomainErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }
    }

    public boolean requiresContent() {
        return this == ETC;
    }
}
