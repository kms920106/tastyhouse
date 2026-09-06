package com.tastyhouse.domain.order.service;

import java.util.List;

public record OrderPlacementItem(
    Long productId,
    Long priceId,
    Integer quantity,
    List<OrderPlacementItemOption> selectedOptions
) {
    public static OrderPlacementItem of(
        Long productId,
        Long priceId,
        Integer quantity,
        List<OrderPlacementItemOption> selectedOptions
    ) {
        return new OrderPlacementItem(productId, priceId, quantity, selectedOptions);
    }
}
