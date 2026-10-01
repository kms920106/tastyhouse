package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;

final class ShopAmenityCategoryMapper {

    private ShopAmenityCategoryMapper() {
    }

    static ShopAmenityCategory toDomain(ShopAmenityCategoryJpaEntity entity) {
        return ShopAmenityCategory.reconstitute(
            entity.getId(),
            entity.getAmenity() == null ? null : Amenity.valueOf(entity.getAmenity()),
            entity.getDisplayName(),
            entity.getActiveImageFileId() == null ? null : UploadedFileId.of(entity.getActiveImageFileId()),
            entity.getInactiveImageFileId() == null ? null : UploadedFileId.of(entity.getInactiveImageFileId()),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopAmenityCategoryJpaEntity toEntity(ShopAmenityCategory shopAmenityCategory) {
        return ShopAmenityCategoryJpaEntity.create(
            shopAmenityCategory.getAmenity() == null ? null : shopAmenityCategory.getAmenity().name(),
            shopAmenityCategory.getDisplayName(),
            shopAmenityCategory.getActiveImageFileId() == null ? null : shopAmenityCategory.getActiveImageFileId().value(),
            shopAmenityCategory.getInactiveImageFileId() == null ? null : shopAmenityCategory.getInactiveImageFileId().value(),
            shopAmenityCategory.getSort(),
            shopAmenityCategory.isVisible()
        );
    }

    static void applyChanges(ShopAmenityCategoryJpaEntity entity, ShopAmenityCategory shopAmenityCategory) {
        entity.applyChanges(
            shopAmenityCategory.getDisplayName(),
            shopAmenityCategory.getActiveImageFileId() == null ? null : shopAmenityCategory.getActiveImageFileId().value(),
            shopAmenityCategory.getInactiveImageFileId() == null ? null : shopAmenityCategory.getInactiveImageFileId().value(),
            shopAmenityCategory.getSort(),
            shopAmenityCategory.isVisible()
        );
    }
}
