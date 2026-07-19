package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;

public record ShopAmenityCategorySaveCommand(
    Amenity amenity,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    boolean visible
) {

    public static ShopAmenityCategorySaveCommand of(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategorySaveCommand(
            amenity,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            visible
        );
    }
}
