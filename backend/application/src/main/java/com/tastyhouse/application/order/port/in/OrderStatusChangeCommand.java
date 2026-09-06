package com.tastyhouse.application.order.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record OrderStatusChangeCommand(
    Long orderId,
    String status
) {
    public OrderStatusChangeCommand {
        if (orderId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
