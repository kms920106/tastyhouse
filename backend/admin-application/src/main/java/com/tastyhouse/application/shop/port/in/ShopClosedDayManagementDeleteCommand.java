package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopClosedDayManagementDeleteCommand(
    Long adminId,
    Long closedDayId
) {

    public ShopClosedDayManagementDeleteCommand {
        if (adminId == null || closedDayId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayManagementDeleteCommand of(Long adminId, Long closedDayId) {
        return new ShopClosedDayManagementDeleteCommand(adminId, closedDayId);
    }
}
