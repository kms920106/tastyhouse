package com.tastyhouse.domain.shop.domain.vo;

public record ShopFoodTypeCategoryId(Long value) {

    public ShopFoodTypeCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ShopFoodTypeCategoryId는 양수여야 합니다: " + value);
        }
    }

    public static ShopFoodTypeCategoryId of(Long value) {
        return new ShopFoodTypeCategoryId(value);
    }
}
