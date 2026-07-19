package com.tastyhouse.core.domain.shop.application.dto.command;

import java.time.LocalTime;

import com.tastyhouse.core.domain.shop.domain.model.DayType;

public record ShopBreakTimeSaveCommand(
    DayType dayType,
    LocalTime startTime,
    LocalTime endTime
) {

    public static ShopBreakTimeSaveCommand of(
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ShopBreakTimeSaveCommand(dayType, startTime, endTime);
    }
}
