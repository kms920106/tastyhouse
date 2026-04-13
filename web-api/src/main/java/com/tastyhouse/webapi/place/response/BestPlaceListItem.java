package com.tastyhouse.webapi.place.response;

import com.tastyhouse.core.entity.place.FoodType;

import java.util.List;

public record BestPlaceListItem(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes
) {
    public static BestPlaceListItem from(
        Long id,
        String name,
        String stationName,
        Double rating,
        String imageUrl,
        List<FoodType> foodTypes
    ) {
        return new BestPlaceListItem(
            id,
            name,
            stationName,
            rating,
            imageUrl,
            foodTypes
        );
    }
}
