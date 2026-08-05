package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopFoodTypeCategoryMapper {

    private ShopFoodTypeCategoryMapper() {
    }

    static ShopFoodTypeCategory toDomain(ShopFoodTypeCategoryJpaEntity entity) {
        return ShopFoodTypeCategory.reconstitute(
            entity.getId(),
            entity.getFoodType(),
            entity.getDisplayName(),
            IdMapping.vo(entity.getActiveImageFileId(), UploadedFileId::of),
            IdMapping.vo(entity.getInactiveImageFileId(), UploadedFileId::of),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopFoodTypeCategoryJpaEntity toEntity(ShopFoodTypeCategory domain) {
        return ShopFoodTypeCategoryJpaEntity.create(
            domain.getFoodType(),
            domain.getDisplayName(),
            IdMapping.raw(domain.getActiveImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getInactiveImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ShopFoodTypeCategoryJpaEntity entity, ShopFoodTypeCategory domain) {
        entity.applyChanges(
            domain.getDisplayName(),
            IdMapping.raw(domain.getActiveImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getInactiveImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
