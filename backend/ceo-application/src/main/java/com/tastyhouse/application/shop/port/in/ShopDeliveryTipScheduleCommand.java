package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopDeliveryTipScheduleCommand(
    String dayType,
    LocalTime startTime,
    LocalTime endTime,
    Integer tipAmount
) {

    public ShopDeliveryTipScheduleCommand {
        if (dayType == null || startTime == null || endTime == null || tipAmount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopDeliveryTipScheduleCommand of(
        String dayType,
        LocalTime startTime,
        LocalTime endTime,
        Integer tipAmount
    ) {
        return new ShopDeliveryTipScheduleCommand(
            dayType,
            startTime,
            endTime,
            tipAmount
        );
    }
}
