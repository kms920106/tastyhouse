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
}
