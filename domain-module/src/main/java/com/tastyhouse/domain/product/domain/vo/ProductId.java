package com.tastyhouse.domain.product.domain.vo;

public record ProductId(Long value) {
    public ProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductId는 양수여야 합니다: " + value);
        }
    }

    public static ProductId of(Long value) {
        return new ProductId(value);
    }
}
