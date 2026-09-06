package com.tastyhouse.domain.product.vo;

public record ProductImageChangeRequestId(Long value) {

    public ProductImageChangeRequestId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(" 양수여야 합니다: " + value);
        }
    }

    public static ProductImageChangeRequestId of(Long value) {
        return new ProductImageChangeRequestId(value);
    }
}
