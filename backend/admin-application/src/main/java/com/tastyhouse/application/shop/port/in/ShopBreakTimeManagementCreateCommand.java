package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBreakTimeManagementCreateCommand(
    Long adminId,
    Long shopId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {

    public ShopBreakTimeManagementCreateCommand {
        if (adminId == null || shopId == null || dayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
