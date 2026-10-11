package com.tastyhouse.application.shop.port.out;

import java.time.LocalTime;

public record ShopDeliveryTipScheduleResult(
    Long id,
    String dayType,
    LocalTime startTime,
    LocalTime endTime,
    int tipAmount
) {
}
