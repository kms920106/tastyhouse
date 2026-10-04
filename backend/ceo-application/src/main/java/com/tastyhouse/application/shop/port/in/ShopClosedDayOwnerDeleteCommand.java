package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopClosedDayOwnerDeleteCommand(
    Long ceoId,
    Long closedDayId
) {

    public ShopClosedDayOwnerDeleteCommand {
        if (ceoId == null || closedDayId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayOwnerDeleteCommand of(Long ceoId, Long closedDayId) {
        return new ShopClosedDayOwnerDeleteCommand(ceoId, closedDayId);
    }
}
