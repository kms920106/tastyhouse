package com.tastyhouse.domain.product.vo;

public record ProductOptionId(Long value) {
    public ProductOptionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductOptionId는 양수여야 합니다: " + value);
        }
    }

    public static ProductOptionId of(Long value) {
        return new ProductOptionId(value);
    }
}
