package com.tastyhouse.infrastructure.shop.query;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

public record BestShopItemResult(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes
) {
    @QueryProjection
    public BestShopItemResult {
    }
}
