package com.tastyhouse.domain.payment.domain.vo;

public record PaymentId(Long value) {

    public PaymentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("PaymentId는 양수여야 합니다: " + value);
        }
    }

    public static PaymentId of(Long value) {
        return new PaymentId(value);
    }
}
