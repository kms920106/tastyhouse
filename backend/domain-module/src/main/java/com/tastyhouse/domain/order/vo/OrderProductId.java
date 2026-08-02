package com.tastyhouse.domain.order.vo;

public record OrderProductId(Long value) {

    public OrderProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderProductId는 양수여야 합니다: " + value);
        }
    }

    public static OrderProductId of(Long value) {
        return new OrderProductId(value);
    }
}
