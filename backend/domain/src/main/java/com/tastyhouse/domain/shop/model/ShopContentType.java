package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ShopContentType {

    IMAGE("이미지"),
    GIF("GIF"),
    VIDEO("동영상");

    private final String description;

    ShopContentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static ShopContentType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_CONTENT_TYPE_UNKNOWN,
                DomainErrorCode.SHOP_CONTENT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
