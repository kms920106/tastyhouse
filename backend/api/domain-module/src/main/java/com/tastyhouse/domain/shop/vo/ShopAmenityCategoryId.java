package com.tastyhouse.domain.shop.vo;

public record ShopAmenityCategoryId(Long value) {

    public ShopAmenityCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ShopAmenityCategoryId는 양수여야 합니다: " + value);
        }
    }

    public static ShopAmenityCategoryId of(Long value) {
        return new ShopAmenityCategoryId(value);
    }
}
