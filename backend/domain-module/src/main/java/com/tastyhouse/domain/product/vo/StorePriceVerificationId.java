package com.tastyhouse.domain.product.vo;

public record StorePriceVerificationId(Long value) {

    public StorePriceVerificationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("StorePriceVerificationId는 양수여야 합니다: " + value);
        }
    }

    public static StorePriceVerificationId of(Long value) {
        return new StorePriceVerificationId(value);
    }
}
