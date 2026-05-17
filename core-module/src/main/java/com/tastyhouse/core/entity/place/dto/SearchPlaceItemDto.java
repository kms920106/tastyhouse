package com.tastyhouse.core.entity.place.dto;

import com.tastyhouse.core.entity.place.FoodType;

import java.util.List;

public record SearchPlaceItemDto(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes,
    Boolean isBookmarked
) {
}
