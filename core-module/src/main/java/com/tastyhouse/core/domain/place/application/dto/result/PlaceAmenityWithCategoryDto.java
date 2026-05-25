package com.tastyhouse.core.domain.place.application.dto.result;

import com.tastyhouse.core.domain.place.domain.model.Amenity;

public record PlaceAmenityWithCategoryDto(
    Amenity amenity,
    String displayName,
    String activeFilePath
) {
}
