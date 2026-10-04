package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBreakTimeManagementUpdateCommand(
    Long adminId,
    Long breakTimeId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {

    public ShopBreakTimeManagementUpdateCommand {
        if (adminId == null || breakTimeId == null || dayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
