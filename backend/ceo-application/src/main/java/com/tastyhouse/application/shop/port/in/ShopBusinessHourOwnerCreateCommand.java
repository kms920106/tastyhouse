package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBusinessHourOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {

    public ShopBusinessHourOwnerCreateCommand {
        if (ceoId == null || shopId == null || dayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
