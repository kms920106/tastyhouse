package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record OrderDeleteCommand(Long orderId) {

    public OrderDeleteCommand {
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static OrderDeleteCommand of(Long orderId) {
        return new OrderDeleteCommand(orderId);
    }
}
