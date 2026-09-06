package com.tastyhouse.domain.product.vo;

public record ProductVegetarianRequestId(Long value) {

    public ProductVegetarianRequestId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(" 양수여야 합니다: " + value);
        }
    }

    public static ProductVegetarianRequestId of(Long value) {
        return new ProductVegetarianRequestId(value);
    }
}
