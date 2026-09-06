package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum OriginSourceType {
    DIRECT("직접 입력"),

    FRANCHISE_URL("본사 제공 URL");

    private final String description;

    OriginSourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static OriginSourceType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN,
                ErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
