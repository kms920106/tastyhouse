package com.tastyhouse.webapi.shop.response;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

import java.util.List;

public record BestShopListItemResponse(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes
) {
    public static BestShopListItemResponse from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        List<FoodType> foodTypes
    ) {
        return new BestShopListItemResponse(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            foodTypes
        );
    }
}
