package com.tastyhouse.domain.shop.vo;

public record ShopPhotoCategoryId(Long value) {

    public ShopPhotoCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ShopPhotoCategoryId는 양수여야 합니다: " + value);
        }
    }

    public static ShopPhotoCategoryId of(Long value) {
        return new ShopPhotoCategoryId(value);
    }
}
