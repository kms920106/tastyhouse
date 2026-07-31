package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum FoodType {

    KOREAN("한식"),
    JAPANESE("일식"),
    WESTERN("양식"),
    CHINESE("중식"),
    WORLD("세계음식"),
    SNACK("분식"),
    BAR("주점"),
    CAFE("카페");

    private final String displayName;

    public static FoodType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.FOOD_TYPE_UNKNOWN,
                ErrorCode.FOOD_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
