package com.tastyhouse.infrastructure.shop.query;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.shop.model.FoodType;

public record BestShopItemResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip
) {
    @QueryProjection
    public BestShopItemResult {
    }
}
