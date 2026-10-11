package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryTipScheduleItemResult(
    String dayType,
    String dayTypeDescription,
    String startTime,
    String endTime,
    int tipAmount
) {
}
