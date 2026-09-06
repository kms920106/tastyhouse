package com.tastyhouse.domain.shop.vo;

public record ShopMenuCollectionImageId(Long value) {
    public ShopMenuCollectionImageId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(" 양수여야 합니다: " + value);
        }
    }

    public static ShopMenuCollectionImageId of(Long value) {
        return new ShopMenuCollectionImageId(value);
    }
}
