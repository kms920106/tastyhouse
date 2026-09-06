package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ProductOptionGroupMergeEntryType {
    RECOMMENDED,
    MANUAL;

    public static ProductOptionGroupMergeEntryType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_ENTRY_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_OPTION_GROUP_MERGE_ENTRY_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
