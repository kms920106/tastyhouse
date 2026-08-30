package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopPhotoCategoryImageMapper {

    private ShopPhotoCategoryImageMapper() {
    }

    static ShopPhotoCategoryImage toDomain(ShopPhotoCategoryImageJpaEntity entity) {
        return ShopPhotoCategoryImage.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopPhotoCategoryId(), ShopPhotoCategoryId::of),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopPhotoCategoryImageJpaEntity toEntity(ShopPhotoCategoryImage domain) {
        return ShopPhotoCategoryImageJpaEntity.create(
            IdMapping.raw(domain.getShopPhotoCategoryId(), ShopPhotoCategoryId::value),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ShopPhotoCategoryImageJpaEntity entity, ShopPhotoCategoryImage domain) {
        entity.applyChanges(
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
