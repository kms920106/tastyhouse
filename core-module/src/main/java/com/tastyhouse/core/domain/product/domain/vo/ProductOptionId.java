package com.tastyhouse.core.domain.product.domain.vo;

public record ProductOptionId(Long value) {
    public ProductOptionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductOptionId must be positive");
        }
    }
}
