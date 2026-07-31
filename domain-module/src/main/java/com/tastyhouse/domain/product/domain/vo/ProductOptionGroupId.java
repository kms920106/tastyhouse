package com.tastyhouse.domain.product.domain.vo;

public record ProductOptionGroupId(Long value) {
    public ProductOptionGroupId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductOptionGroupId는 양수여야 합니다: " + value);
        }
    }

    public static ProductOptionGroupId of(Long value) {
        return new ProductOptionGroupId(value);
    }
}
