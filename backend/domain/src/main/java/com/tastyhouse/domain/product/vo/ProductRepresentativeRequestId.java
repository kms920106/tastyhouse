package com.tastyhouse.domain.product.vo;

public record ProductRepresentativeRequestId(Long value) {

    public ProductRepresentativeRequestId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(" 양수여야 합니다: " + value);
        }
    }

    public static ProductRepresentativeRequestId of(Long value) {
        return new ProductRepresentativeRequestId(value);
    }
}
