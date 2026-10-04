package com.tastyhouse.domain.shop.model;

public record ShopDeliveryTipRegionSpec(Long adminDongId, int tipAmount) {

    public static ShopDeliveryTipRegionSpec of(Long adminDongId, int tipAmount) {
        return new ShopDeliveryTipRegionSpec(adminDongId, tipAmount);
    }
}
