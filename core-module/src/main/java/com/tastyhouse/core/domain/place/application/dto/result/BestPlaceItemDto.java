package com.tastyhouse.core.domain.place.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;
import com.tastyhouse.core.domain.place.domain.model.FoodType;

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
