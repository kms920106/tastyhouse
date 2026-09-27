package com.tastyhouse.application.shop.port.out;

public record ShopOrderMethodResult(
    Long id,
    String orderMethod,
    String orderMethodDisplayName
) {
}
