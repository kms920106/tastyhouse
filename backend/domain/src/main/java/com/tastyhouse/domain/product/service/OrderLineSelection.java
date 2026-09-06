package com.tastyhouse.domain.product.service;

import java.util.List;

public record OrderLineSelection(
    Long productId,
    Long priceId,
    int quantity,
    List<OrderLineOptionSelection> selectedOptions
) {
    public OrderLineSelection {
        selectedOptions = selectedOptions == null ? List.of() : List.copyOf(selectedOptions);
    }

    public static OrderLineSelection of(
        Long productId,
        Long priceId,
        int quantity,
        List<OrderLineOptionSelection> selectedOptions
    ) {
        return new OrderLineSelection(productId, priceId, quantity, selectedOptions);
    }
}
