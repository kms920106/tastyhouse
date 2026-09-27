package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

public record ShopBreakTimeResult(
    Long id,
    String dayType,
    String dayTypeDescription,
    LocalTime startTime,
    LocalTime endTime
) {

    public ShopBreakTimeResult withDayTypeDescription(String dayTypeDescription) {
        return new ShopBreakTimeResult(
            this.id,
            this.dayType,
            dayTypeDescription,
            this.startTime,
            this.endTime
        );
    }
}
