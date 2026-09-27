package com.tastyhouse.application.shop.port.out.write;

public record ShopDeliveryTipHolidayState(
    Long id,
    Long shopId,
    int tipAmount
) {
}
