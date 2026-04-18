package com.tastyhouse.core.entity.place.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.entity.place.FoodType;

import java.util.List;

public record BestPlaceItemDto(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes
) {
    @QueryProjection
    public BestPlaceItemDto {
    }
}
