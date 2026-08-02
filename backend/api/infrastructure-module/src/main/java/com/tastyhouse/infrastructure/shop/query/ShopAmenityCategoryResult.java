package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.domain.shop.model.Amenity;

public record ShopAmenityCategoryResult(
    Long id,
    Amenity amenity,
    String displayName,
    String activeIconUrl,
    String inactiveIconUrl,
    Integer sort,
    boolean visible
) {
}
