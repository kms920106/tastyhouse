package com.tastyhouse.domain.shop.service;

public record ShopDeliveryResolution(
    int distanceMeters,
    ShopDeliveryTipBreakdown tipBreakdown
) {
}
