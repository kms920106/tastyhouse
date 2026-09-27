package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalTime;

public record ShopBreakTimeState(
    Long id,
    Long shopId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime
) {
}
