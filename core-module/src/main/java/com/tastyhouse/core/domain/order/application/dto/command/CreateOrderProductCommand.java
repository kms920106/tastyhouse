package com.tastyhouse.core.domain.order.application.dto.command;

import java.util.List;

public record CreateOrderProductCommand(
    Long productId,
    Integer quantity,
    List<CreateOrderProductOptionCommand> selectedOptions
) {

    public static CreateOrderProductCommand of(
        Long productId,
        Integer quantity,
        List<CreateOrderProductOptionCommand> selectedOptions
    ) {
        return new CreateOrderProductCommand(productId, quantity, selectedOptions);
    }
}
