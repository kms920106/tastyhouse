package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

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
            throw new DomainException(DomainErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN,
                DomainErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
