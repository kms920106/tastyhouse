package com.tastyhouse.core.entity.place.dto;

import com.tastyhouse.core.entity.place.FoodType;

public record PlaceFoodTypeCategoryDto(
    Long id,
    FoodType foodType,
    String displayName,
    String activeFilePath,
    String inactiveFilePath,
    Integer sort,
    Boolean isActive
) {
}
