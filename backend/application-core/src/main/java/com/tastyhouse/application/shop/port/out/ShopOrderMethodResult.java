package com.tastyhouse.application.shop.port.out;

public record ShopOrderMethodResult(
    Long id,
    String orderMethod,
    String orderMethodDisplayName
) {

    public ShopOrderMethodResult withOrderMethodDisplayName(String orderMethodDisplayName) {
        return new ShopOrderMethodResult(
            this.id,
            this.orderMethod,
            orderMethodDisplayName
        );
    }
}
