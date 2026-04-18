package com.tastyhouse.core.entity.place.dto;

import com.tastyhouse.core.entity.place.FoodType;

import java.time.LocalDateTime;
import java.util.List;

public record LatestPlaceItemDto(
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
}
