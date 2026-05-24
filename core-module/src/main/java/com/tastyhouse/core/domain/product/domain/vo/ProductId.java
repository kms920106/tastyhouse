package com.tastyhouse.core.domain.product.domain.vo;

public record ProductId(Long value) {
    public ProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductId must be positive");
        }
    }
}
