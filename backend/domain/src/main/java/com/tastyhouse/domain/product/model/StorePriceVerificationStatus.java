package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum StorePriceVerificationStatus {
    PENDING("대기"),
    IN_PROGRESS("검수 중"),
    APPROVED("승인"),
    REJECTED("반려"),
    CANCELED("취소");

    private final String description;

    StorePriceVerificationStatus(String description) {
        this.description = description;
    }

    public static StorePriceVerificationStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_STORE_PRICE_VERIFICATION_STATUS_UNKNOWN,
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public boolean isOpen() {
        return this == PENDING || this == IN_PROGRESS;
    }

    public String getDescription() {
        return this.description;
    }
}
