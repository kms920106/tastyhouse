package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

import com.tastyhouse.domain.shared.model.DayType;

public record ShopBusinessHourResult(
    Long id,
    DayType dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean closed,
    Boolean allDay
) {
}
