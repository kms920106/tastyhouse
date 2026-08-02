package com.tastyhouse.domain.order.vo;

public record OrderId(Long value) {

    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderId는 양수여야 합니다: " + value);
        }
    }

    public static OrderId of(Long value) {
        return new OrderId(value);
    }
}
