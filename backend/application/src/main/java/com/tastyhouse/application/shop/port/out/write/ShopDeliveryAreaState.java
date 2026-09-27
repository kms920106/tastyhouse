package com.tastyhouse.application.shop.port.out.write;

public record ShopDeliveryAreaState(
    Long id,
    Long shopId,
    Long adminDongId,
    String source
) {
}
