package com.tastyhouse.application.shop.port.out;

public record ShopClosedDayResult(
    Long id,
    String closedDayType,
    String closedDayTypeDescription
) {
}
