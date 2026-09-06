package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

import com.tastyhouse.domain.shared.model.DayType;

public record ShopBreakTimeResult(
    Long id,
    DayType dayType,
    LocalTime startTime,
    LocalTime endTime
) {
}
