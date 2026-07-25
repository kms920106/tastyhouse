package com.tastyhouse.core.domain.shop.application.dto.command;

import java.time.LocalTime;

import com.tastyhouse.core.domain.shop.domain.model.DayType;

public record ShopBusinessHourSaveCommand(
    DayType dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {

    public static ShopBusinessHourSaveCommand of(
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHourSaveCommand(dayType, openTime, closeTime, isClosed, is24Hours);
    }
}
