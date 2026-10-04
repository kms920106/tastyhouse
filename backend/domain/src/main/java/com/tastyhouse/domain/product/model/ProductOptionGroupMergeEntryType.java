package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ProductOptionGroupMergeEntryType {

    RECOMMENDED,
    MANUAL;

    public static ProductOptionGroupMergeEntryType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainException(DomainErrorCode.PRODUCT_OPTION_GROUP_MERGE_ENTRY_TYPE_UNKNOWN,
                DomainErrorCode.PRODUCT_OPTION_GROUP_MERGE_ENTRY_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
