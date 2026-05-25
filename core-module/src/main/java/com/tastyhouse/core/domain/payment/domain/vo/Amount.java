package com.tastyhouse.core.domain.payment.domain.vo;

public record Amount(Integer value) {

    public Amount {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Amount는 0 이상이어야 합니다: " + value);
        }
    }
}
