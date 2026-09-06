package com.tastyhouse.application.order.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record OrderDeleteCommand(Long orderId) {
    public OrderDeleteCommand {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static OrderDeleteCommand of(Long orderId) {
        return new OrderDeleteCommand(orderId);
    }
}
