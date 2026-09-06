package com.tastyhouse.domain.shop.service;

public record ShopDeliveryTipTierSpec(int minOrderAmount, int tipAmount) {
    public static ShopDeliveryTipTierSpec of(int minOrderAmount, int tipAmount) {
        return new ShopDeliveryTipTierSpec(minOrderAmount, tipAmount);
    }
}
