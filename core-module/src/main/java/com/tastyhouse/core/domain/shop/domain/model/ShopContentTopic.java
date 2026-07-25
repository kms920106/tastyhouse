package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ShopContentTopic {

    EXTERIOR("가게 외부"),
    INTERIOR("가게 내부"),
    FOOD_STORY("음식 스토리"),
    NEWS("가게 소식");

    private final String description;

    public static ShopContentTopic from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_TOPIC_UNKNOWN,
                ErrorCode.SHOP_CONTENT_TOPIC_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
