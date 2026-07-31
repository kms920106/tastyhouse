package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.domain.model.Amenity;

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
