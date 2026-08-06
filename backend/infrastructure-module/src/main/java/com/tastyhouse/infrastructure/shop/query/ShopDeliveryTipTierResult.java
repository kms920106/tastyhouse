package com.tastyhouse.infrastructure.shop.query;

/** 구간별 배달팁 한 행(표현용). */
public record ShopDeliveryTipTierResult(Long id, int tierOrder, int minOrderAmount, int tipAmount) {
}
