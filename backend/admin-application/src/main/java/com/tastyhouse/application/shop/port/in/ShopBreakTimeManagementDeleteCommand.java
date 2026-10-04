package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBreakTimeManagementDeleteCommand(
    Long adminId,
    Long breakTimeId
) {

    public ShopBreakTimeManagementDeleteCommand {
        if (adminId == null || breakTimeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBreakTimeManagementDeleteCommand of(Long adminId, Long breakTimeId) {
        return new ShopBreakTimeManagementDeleteCommand(adminId, breakTimeId);
    }
}
