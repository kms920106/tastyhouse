package com.tastyhouse.core.domain.place.application.dto.result;

import com.tastyhouse.core.domain.place.domain.model.Amenity;

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
