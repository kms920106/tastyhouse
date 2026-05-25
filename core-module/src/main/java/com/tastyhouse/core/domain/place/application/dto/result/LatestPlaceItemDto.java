package com.tastyhouse.core.domain.place.application.dto.result;

import com.tastyhouse.core.domain.place.domain.model.FoodType;

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
