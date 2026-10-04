package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record OrderLineOptionCommand(
    Long groupId,
    Long optionId
) {

    public OrderLineOptionCommand {
        if (groupId == null || optionId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static OrderLineOptionCommand of(Long groupId, Long optionId) {
        return new OrderLineOptionCommand(groupId, optionId);
    }
}
