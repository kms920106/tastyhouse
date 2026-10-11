package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryTipRangeResult(Long shopId, int minDeliveryTip, int maxDeliveryTip) {

    public static ShopDeliveryTipRangeResult none(Long shopId) {
        return new ShopDeliveryTipRangeResult(shopId, 0, 0);
    }
}
