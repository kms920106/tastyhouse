package com.tastyhouse.application.shop.port.out.write;

public record ShopOrderMethodState(
    Long id,
    Long shopId,
    String orderMethod
) {
}
