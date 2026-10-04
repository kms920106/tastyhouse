package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBusinessHourOwnerUpdateCommand(
    Long ceoId,
    Long businessHourId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {

    public ShopBusinessHourOwnerUpdateCommand {
        if (ceoId == null || businessHourId == null || dayType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
