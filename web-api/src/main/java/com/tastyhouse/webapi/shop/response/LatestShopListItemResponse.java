package com.tastyhouse.webapi.shop.response;

import com.tastyhouse.core.domain.shop.domain.model.FoodType;

import java.time.LocalDateTime;
import java.util.List;

public record LatestShopListItemResponse(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    LocalDateTime createdAt,
    Long reviewCount,
    Long bookmarkCount,
    List<FoodType> foodTypes
) {
    public static LatestShopListItemResponse from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        LocalDateTime createdAt,
        Long reviewCount,
        Long bookmarkCount,
        List<FoodType> foodTypes
    ) {
        return new LatestShopListItemResponse(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            createdAt,
            reviewCount,
            bookmarkCount,
            foodTypes
        );
    }
}
