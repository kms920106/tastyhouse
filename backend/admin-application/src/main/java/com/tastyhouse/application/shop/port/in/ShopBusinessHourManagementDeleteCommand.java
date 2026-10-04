package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBusinessHourManagementDeleteCommand(
    Long adminId,
    Long businessHourId
) {

    public ShopBusinessHourManagementDeleteCommand {
        if (adminId == null || businessHourId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBusinessHourManagementDeleteCommand of(Long adminId, Long businessHourId) {
        return new ShopBusinessHourManagementDeleteCommand(adminId, businessHourId);
    }
}
