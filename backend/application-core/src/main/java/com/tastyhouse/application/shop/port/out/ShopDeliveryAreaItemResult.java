package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryAreaItemResult(
    long id,
    long adminDongId,
    String regionName,
    String source
) {
}
