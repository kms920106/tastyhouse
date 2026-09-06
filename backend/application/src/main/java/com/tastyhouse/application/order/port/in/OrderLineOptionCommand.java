package com.tastyhouse.application.order.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record OrderLineOptionCommand(
    Long groupId,
    Long optionId
) {
    public OrderLineOptionCommand {
        if (groupId == null || optionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static OrderLineOptionCommand of(Long groupId, Long optionId) {
        return new OrderLineOptionCommand(groupId, optionId);
    }
}
