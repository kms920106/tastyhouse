package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum DeliveryTipExtraType {
    NONE("미사용"),

    DISTANCE("거리별"),

    REGION("지역별");

    private final String description;

    DeliveryTipExtraType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static DeliveryTipExtraType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DELIVERY_TIP_EXTRA_TYPE_UNKNOWN,
                ErrorCode.DELIVERY_TIP_EXTRA_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
