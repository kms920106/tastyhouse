package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopAmenityCategoryMapper {

    private ShopAmenityCategoryMapper() {
    }

    static ShopAmenityCategory toDomain(ShopAmenityCategoryJpaEntity entity) {
        return ShopAmenityCategory.reconstitute(
            entity.getId(),
            entity.getAmenity(),
            entity.getDisplayName(),
            IdMapping.vo(entity.getActiveImageFileId(), UploadedFileId::of),
            IdMapping.vo(entity.getInactiveImageFileId(), UploadedFileId::of),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopAmenityCategoryJpaEntity toEntity(ShopAmenityCategory domain) {
        return ShopAmenityCategoryJpaEntity.create(
            domain.getAmenity(),
            domain.getDisplayName(),
            IdMapping.raw(domain.getActiveImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getInactiveImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ShopAmenityCategoryJpaEntity entity, ShopAmenityCategory domain) {
        entity.applyChanges(
            domain.getDisplayName(),
            IdMapping.raw(domain.getActiveImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getInactiveImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
