package com.tastyhouse.domain.product.domain.vo;

public record ProductCategoryId(Long value) {
    public ProductCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductCategoryId는 양수여야 합니다: " + value);
        }
    }

    public static ProductCategoryId of(Long value) {
        return new ProductCategoryId(value);
    }
}
