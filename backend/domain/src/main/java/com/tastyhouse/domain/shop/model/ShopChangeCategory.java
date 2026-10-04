package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ShopChangeCategory {

    OPERATION("운영 정보"),
    DELIVERY("배달 정보"),
    SHOP_INFO("가게 정보"),
    IMAGE("이미지·상표"),
    RIDER("라이더 안내");

    private final String description;

    ShopChangeCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static ShopChangeCategory from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_CHANGE_CATEGORY_UNKNOWN,
                DomainErrorCode.SHOP_CHANGE_CATEGORY_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
