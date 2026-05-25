package com.tastyhouse.core.domain.place.application.dto.result;

import com.tastyhouse.core.domain.place.domain.model.FoodType;

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
