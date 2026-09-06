package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ProductFeedbackType {
    PRICE,

    IMAGE,

    COMPOSITION,

    SOLD_OUT,

    ETC;

    public static ProductFeedbackType from(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_TYPE_UNKNOWN);
        }
    }

    public boolean requiresContent() {
        return this == ETC;
    }
}
