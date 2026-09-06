package com.tastyhouse.domain.product.vo;

public record ProductPriceId(Long value) {

    public ProductPriceId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductPriceId는 양수여야 합니다: " + value);
        }
    }

    public static ProductPriceId of(Long value) {
        return new ProductPriceId(value);
    }
}
