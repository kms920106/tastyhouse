package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;

final class ShopPhotoCategoryImageMapper {

    private ShopPhotoCategoryImageMapper() {
    }

    static ShopPhotoCategoryImage toDomain(ShopPhotoCategoryImageJpaEntity entity) {
        return ShopPhotoCategoryImage.reconstitute(
            entity.getId(),
            entity.getShopPhotoCategoryId() == null ? null : ShopPhotoCategoryId.of(entity.getShopPhotoCategoryId()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopPhotoCategoryImageJpaEntity toEntity(ShopPhotoCategoryImage shopPhotoCategoryImage) {
        return ShopPhotoCategoryImageJpaEntity.create(
            shopPhotoCategoryImage.getShopPhotoCategoryId() == null ? null : shopPhotoCategoryImage.getShopPhotoCategoryId().value(),
            shopPhotoCategoryImage.getImageFileId() == null ? null : shopPhotoCategoryImage.getImageFileId().value(),
            shopPhotoCategoryImage.getSort(),
            shopPhotoCategoryImage.isVisible()
        );
    }

    static void applyChanges(ShopPhotoCategoryImageJpaEntity entity, ShopPhotoCategoryImage shopPhotoCategoryImage) {
        entity.applyChanges(
            shopPhotoCategoryImage.getImageFileId() == null ? null : shopPhotoCategoryImage.getImageFileId().value(),
            shopPhotoCategoryImage.getSort(),
            shopPhotoCategoryImage.isVisible()
        );
    }
}
