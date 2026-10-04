package com.tastyhouse.domain.shop.model;

public record ShopDeliveryResolution(
    int distanceMeters,
    ShopDeliveryTipBreakdown tipBreakdown
) {
}
