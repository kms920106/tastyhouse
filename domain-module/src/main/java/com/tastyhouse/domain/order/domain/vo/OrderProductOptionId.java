package com.tastyhouse.domain.order.domain.vo;

public record OrderProductOptionId(Long value) {

    public OrderProductOptionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("OrderProductOptionId는 양수여야 합니다: " + value);
        }
    }

    public static OrderProductOptionId of(Long value) {
        return new OrderProductOptionId(value);
    }
}
