package com.tastyhouse.core.domain.shop.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;

import java.util.List;

public record BestShopItemDto(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes
) {
    @QueryProjection
    public BestShopItemDto {
    }
}
