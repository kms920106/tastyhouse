package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalTime;

public record ShopBusinessHourState(
    Long id,
    Long shopId,
    String dayType,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean isClosed,
    Boolean is24Hours
) {
}
