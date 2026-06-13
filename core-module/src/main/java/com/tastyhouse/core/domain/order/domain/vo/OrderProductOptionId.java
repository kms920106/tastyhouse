package com.tastyhouse.core.domain.order.domain.vo;

public record OrderItemOptionId(Long value) {

    public OrderItemOptionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderItemOptionId는 양수여야 합니다: " + value);
        }
    }
}
