package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryAreaBlockedView(
    long adminDongId,
    String regionName,
    String reason
) {
}
