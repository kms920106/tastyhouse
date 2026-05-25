package com.tastyhouse.webapi.place.response;

import com.tastyhouse.core.domain.place.domain.model.FoodType;

import java.time.LocalDateTime;
import java.util.List;

public record LatestPlaceListItemResponse(
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
    public static LatestPlaceListItemResponse from(
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
    return new LatestPlaceListItemResponse(
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
