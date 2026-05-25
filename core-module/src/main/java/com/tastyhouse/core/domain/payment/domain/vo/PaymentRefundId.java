package com.tastyhouse.core.domain.payment.domain.vo;

public record PaymentRefundId(Long value) {

    public PaymentRefundId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("PaymentRefundId는 양수여야 합니다: " + value);
        }
    }
}
