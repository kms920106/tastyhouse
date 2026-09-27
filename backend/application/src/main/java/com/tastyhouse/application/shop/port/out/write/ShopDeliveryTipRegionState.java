package com.tastyhouse.application.shop.port.out.write;

public record ShopDeliveryTipRegionState(
    Long id,
    Long shopId,
    Long adminDongId,
    int tipAmount
) {
}
