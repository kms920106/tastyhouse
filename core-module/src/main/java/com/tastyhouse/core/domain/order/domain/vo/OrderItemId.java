package com.tastyhouse.core.domain.order.domain.vo;

public record OrderItemId(Long value) {

    public OrderItemId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderItemId는 양수여야 합니다: " + value);
        }
    }
}
