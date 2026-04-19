package com.tastyhouse.core.entity.place.dto;

import com.tastyhouse.core.entity.place.Amenity;

public record PlaceAmenityWithCategoryDto(
    Amenity amenity,
    String displayName,
    String activeFilePath
) {
}
