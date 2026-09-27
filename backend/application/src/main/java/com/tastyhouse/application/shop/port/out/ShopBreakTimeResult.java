package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

public record ShopBreakTimeResult(
    Long id,
    String dayType,
    String dayTypeDescription,
    LocalTime startTime,
    LocalTime endTime
) {
}
