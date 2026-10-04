package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopClosedDayOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String closedDayType
) {

    public ShopClosedDayOwnerCreateCommand {
        if (ceoId == null || shopId == null || closedDayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
