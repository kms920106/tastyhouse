package com.tastyhouse.application.shop.port.in;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopDeliveryTipScheduleCommand(
    String dayType,
    LocalTime startTime,
    LocalTime endTime,
    Integer tipAmount
) {
    public ShopDeliveryTipScheduleCommand {
        if (dayType == null || startTime == null || endTime == null || tipAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
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
