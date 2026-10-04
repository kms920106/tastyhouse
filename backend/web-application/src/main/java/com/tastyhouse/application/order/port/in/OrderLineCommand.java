package com.tastyhouse.application.order.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record OrderLineCommand(
    Long productId,
    Long priceId,
    List<OrderLineOptionCommand> options,
    Integer quantity
) {

    public OrderLineCommand {
        if (productId == null || quantity == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static OrderLineCommand of(
        Long productId,
        Long priceId,
        List<OrderLineOptionCommand> options,
        Integer quantity
    ) {
        return new OrderLineCommand(
            productId,
            priceId,
            options,
            quantity
        );
    }
}
