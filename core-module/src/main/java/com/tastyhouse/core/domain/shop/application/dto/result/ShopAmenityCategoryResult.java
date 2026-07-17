package com.tastyhouse.core.domain.shop.application.dto.result;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;

public record ShopAmenityCategoryResult(
    Long id,
    Amenity amenity,
    String displayName,
    String activeFilePath,
    String inactiveFilePath,
    Integer sort,
    boolean visible
) {
}
