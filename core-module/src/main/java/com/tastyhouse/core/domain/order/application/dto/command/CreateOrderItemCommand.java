package com.tastyhouse.core.domain.order.application.dto.command;

import java.util.List;

public record CreateOrderItemCommand(
    Long productId,
    Integer quantity,
    List<CreateOrderItemOptionCommand> selectedOptions
) {
}
