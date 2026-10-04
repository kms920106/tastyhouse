package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBreakTimeOwnerUpdateCommand(
    Long ceoId,
    Long breakTimeId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {

    public ShopBreakTimeOwnerUpdateCommand {
        if (ceoId == null || breakTimeId == null || dayType == null || startTime == null || endTime == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
