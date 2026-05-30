package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}
