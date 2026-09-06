package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ProductOptionGroupType {
    NORMAL,
    CUP_DEPOSIT;

    public static ProductOptionGroupType from(String code) {
        if (code == null || code.isBlank()) {
            return NORMAL;
        }
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_OPTION_GROUP_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public boolean isCupDeposit() {
        return this == CUP_DEPOSIT;
    }
}
