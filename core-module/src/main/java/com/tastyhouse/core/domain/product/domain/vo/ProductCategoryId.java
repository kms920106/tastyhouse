package com.tastyhouse.core.domain.product.domain.vo;

public record ProductCategoryId(Long value) {
    public ProductCategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductCategoryId must be positive");
        }
    }
}
