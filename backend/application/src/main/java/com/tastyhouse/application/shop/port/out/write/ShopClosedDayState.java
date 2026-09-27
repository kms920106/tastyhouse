package com.tastyhouse.application.shop.port.out.write;

public record ShopClosedDayState(
    Long id,
    Long shopId,
    String closedDayType
) {
}
