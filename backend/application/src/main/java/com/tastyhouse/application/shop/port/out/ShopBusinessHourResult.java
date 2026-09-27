package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

public record ShopBusinessHourResult(
    Long id,
    String dayType,
    String dayTypeDescription,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean closed,
    Boolean allDay
) {

    public ShopBusinessHourResult withDayTypeDescription(String dayTypeDescription) {
        return new ShopBusinessHourResult(
            this.id,
            this.dayType,
            dayTypeDescription,
            this.openTime,
            this.closeTime,
            this.closed,
            this.allDay
        );
    }
}
