package com.tastyhouse.core.domain.shop.domain.vo;

public record ShopId(Long value) {

    public ShopId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ShopId는 양수여야 합니다: " + value);
        }
    }

    public static ShopId of(Long value) {
        return new ShopId(value);
    }
}
