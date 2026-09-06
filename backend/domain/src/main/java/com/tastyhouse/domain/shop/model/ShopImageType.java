package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ShopImageType {

    TRADEMARK("상표"),
    THUMBNAIL("대표이미지");

    private final String description;

    ShopImageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static ShopImageType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_TYPE_UNKNOWN,
                ErrorCode.SHOP_IMAGE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
