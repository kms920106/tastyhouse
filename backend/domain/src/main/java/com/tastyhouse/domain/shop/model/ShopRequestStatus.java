package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ShopRequestStatus {
    PENDING("대기중"),
    IN_PROGRESS("진행"),
    REJECTED("반려"),
    CANCELED("취소"),
    APPROVED("승인");

    private final String description;

    ShopRequestStatus(String description) {
        this.description = description;
    }

    public static ShopRequestStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_STATUS_UNKNOWN,
                ErrorCode.SHOP_REQUEST_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public boolean isOpen() {
        return this == PENDING || this == IN_PROGRESS;
    }

    public boolean isClosed() {
        return !isOpen();
    }

    public String getDescription() {
        return this.description;
    }
}
