package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum ShopContentTopic {

    EXTERIOR("가게 외부"),
    INTERIOR("가게 내부"),
    FOOD_STORY("음식 스토리"),
    NEWS("가게 소식");

    private final String description;

    ShopContentTopic(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static ShopContentTopic from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.SHOP_CONTENT_TOPIC_UNKNOWN,
                DomainErrorCode.SHOP_CONTENT_TOPIC_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
