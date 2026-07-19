package com.tastyhouse.core.domain.shop.application.dto.command;

import java.time.LocalTime;

import com.tastyhouse.core.domain.shop.domain.model.DayType;

public record ShopBusinessHourSaveCommand(
    DayType dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed
) {

    public static ShopBusinessHourSaveCommand of(
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed
    ) {
        return new ShopBusinessHourSaveCommand(dayType, openTime, closeTime, isClosed);
    }
}
