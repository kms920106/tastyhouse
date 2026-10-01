package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopPhotoCategoryMapper {

    private ShopPhotoCategoryMapper() {
    }

    static ShopPhotoCategory toDomain(ShopPhotoCategoryJpaEntity entity) {
        return ShopPhotoCategory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getName()
        );
    }

    static ShopPhotoCategoryJpaEntity toEntity(ShopPhotoCategory shopPhotoCategory) {
        return ShopPhotoCategoryJpaEntity.create(
            shopPhotoCategory.getShopId() == null ? null : shopPhotoCategory.getShopId().value(),
            shopPhotoCategory.getName()
        );
    }

    static void applyChanges(ShopPhotoCategoryJpaEntity entity, ShopPhotoCategory shopPhotoCategory) {
        entity.applyChanges(shopPhotoCategory.getName());
    }
}
