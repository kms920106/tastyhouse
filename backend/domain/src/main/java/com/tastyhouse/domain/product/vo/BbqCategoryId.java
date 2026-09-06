package com.tastyhouse.domain.product.vo;

public record BbqCategoryId(Long value) {

    public BbqCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("BbqCategoryId는 양수여야 합니다: " + value);
        }
    }

    public static BbqCategoryId of(Long value) {
        return new BbqCategoryId(value);
    }
}
