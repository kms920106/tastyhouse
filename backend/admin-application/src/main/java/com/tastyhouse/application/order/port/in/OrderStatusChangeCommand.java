package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record OrderStatusChangeCommand(
    Long orderId,
    String status
) {

    public OrderStatusChangeCommand {
        if (orderId == null || status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
