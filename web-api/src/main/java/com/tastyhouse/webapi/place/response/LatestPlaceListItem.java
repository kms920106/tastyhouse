package com.tastyhouse.webapi.place.response;

import com.tastyhouse.core.entity.place.FoodType;

import java.time.LocalDateTime;
import java.util.List;

public record LatestPlaceListItem(
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
    public static LatestPlaceListItem from(
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
        return new LatestPlaceListItem(
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
