package com.tastyhouse.core.entity.place.dto;

import com.tastyhouse.core.entity.place.Amenity;

public record PlaceAmenityCategoryDto(
    Long id,
    Amenity amenity,
    String displayName,
    String activeFilePath,
    String inactiveFilePath,
    Integer sort,
    Boolean isActive
) {
}
