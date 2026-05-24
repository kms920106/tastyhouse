package com.tastyhouse.core.domain.product.domain.vo;

public record ProductOptionGroupId(Long value) {
    public ProductOptionGroupId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductOptionGroupId must be positive");
        }
    }
}
