package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryTipTierResult(Long id, int tierOrder, int minOrderAmount, int tipAmount) {
}
