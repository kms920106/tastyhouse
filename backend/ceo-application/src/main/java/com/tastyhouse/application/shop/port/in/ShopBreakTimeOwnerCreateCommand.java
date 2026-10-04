package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBreakTimeOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {

    public ShopBreakTimeOwnerCreateCommand {
        if (ceoId == null || shopId == null || dayType == null || startTime == null || endTime == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
