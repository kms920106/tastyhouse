package com.tastyhouse.application.shop.port.out.write;

public record ShopDeliveryTipTierState(
    Long id,
    Long shopId,
    int tierOrder,
    int minOrderAmount,
    int tipAmount
) {
}
