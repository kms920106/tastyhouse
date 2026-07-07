package com.tastyhouse.core.domain.order.application.dto.command;

import java.util.List;

public record OrderProductCreateCommand(
    Long productId,
    Integer quantity,
    List<OrderProductOptionCreateCommand> selectedOptions
) {

    public static OrderProductCreateCommand of(
        Long productId,
        Integer quantity,
        List<OrderProductOptionCreateCommand> selectedOptions
    ) {
        return new OrderProductCreateCommand(productId, quantity, selectedOptions);
    }
}
