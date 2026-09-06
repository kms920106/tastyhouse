package com.tastyhouse.domain.product.vo;

public record ProductCommonOptionId(Long value) {
    public ProductCommonOptionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductCommonOptionId는 양수여야 합니다: " + value);
        }
    }

    public static ProductCommonOptionId of(Long value) {
        return new ProductCommonOptionId(value);
    }
}
