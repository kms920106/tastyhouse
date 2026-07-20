package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategoryImage;

final class ShopPhotoCategoryImageMapper {

    private ShopPhotoCategoryImageMapper() {
    }

    static ShopPhotoCategoryImage toDomain(ShopPhotoCategoryImageJpaEntity entity) {
        return ShopPhotoCategoryImage.reconstitute(
            entity.getId(),
            entity.getShopPhotoCategoryId(),
            entity.getImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    static ShopPhotoCategoryImageJpaEntity toEntity(ShopPhotoCategoryImage domain) {
        return ShopPhotoCategoryImageJpaEntity.create(
            domain.getShopPhotoCategoryId(),
            domain.getImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }

    static void applyChanges(ShopPhotoCategoryImageJpaEntity entity, ShopPhotoCategoryImage domain) {
        entity.applyChanges(
            domain.getImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
