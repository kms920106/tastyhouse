package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.application.shop.port.out.write.ShopAmenityCategoryState;

final class ShopAmenityCategoryStateMapper {
    private ShopAmenityCategoryStateMapper() {
    }

    static ShopAmenityCategory toDomain(ShopAmenityCategoryState state) {
        return ShopAmenityCategory.reconstitute(
            state.id(),
            state.amenity() == null ? null : Amenity.valueOf(state.amenity()),
            state.displayName(),
            state.activeImageFileId() == null ? null : UploadedFileId.of(state.activeImageFileId()),
            state.inactiveImageFileId() == null ? null : UploadedFileId.of(state.inactiveImageFileId()),
            state.sort(),
            state.visible()
        );
    }

    static ShopAmenityCategoryState toState(ShopAmenityCategory shopAmenityCategory) {
        return new ShopAmenityCategoryState(
            shopAmenityCategory.getId(),
            shopAmenityCategory.getAmenity() == null ? null : shopAmenityCategory.getAmenity().name(),
            shopAmenityCategory.getDisplayName(),
            shopAmenityCategory.getActiveImageFileId() == null ? null : shopAmenityCategory.getActiveImageFileId().value(),
            shopAmenityCategory.getInactiveImageFileId() == null ? null : shopAmenityCategory.getInactiveImageFileId().value(),
            shopAmenityCategory.getSort(),
            shopAmenityCategory.isVisible()
        );
    }
}
