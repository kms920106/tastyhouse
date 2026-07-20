package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;

final class ShopAmenityCategoryMapper {

    private ShopAmenityCategoryMapper() {
    }

    static ShopAmenityCategory toDomain(ShopAmenityCategoryJpaEntity entity) {
        return ShopAmenityCategory.reconstitute(
            entity.getId(),
            entity.getAmenity(),
            entity.getDisplayName(),
            entity.getActiveImageFileId(),
            entity.getInactiveImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopAmenityCategoryJpaEntity toEntity(ShopAmenityCategory domain) {
        return ShopAmenityCategoryJpaEntity.create(
            domain.getAmenity(),
            domain.getDisplayName(),
            domain.getActiveImageFileId(),
            domain.getInactiveImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ShopAmenityCategoryJpaEntity entity, ShopAmenityCategory domain) {
        entity.applyChanges(
            domain.getDisplayName(),
            domain.getActiveImageFileId(),
            domain.getInactiveImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
