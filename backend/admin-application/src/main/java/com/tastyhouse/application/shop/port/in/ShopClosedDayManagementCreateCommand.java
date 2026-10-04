package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopClosedDayManagementCreateCommand(
    Long adminId,
    Long shopId,
    String closedDayType
) {

    public ShopClosedDayManagementCreateCommand {
        if (adminId == null || shopId == null || closedDayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
