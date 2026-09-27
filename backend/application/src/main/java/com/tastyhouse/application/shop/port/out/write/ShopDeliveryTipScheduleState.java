package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalTime;

public record ShopDeliveryTipScheduleState(
    Long id,
    Long shopId,
    String dayType,
    LocalTime startTime,
    LocalTime endTime,
    int tipAmount
) {
}
