package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.domain.model.ShopPhotoCategory;

final class ShopPhotoCategoryMapper {

    private ShopPhotoCategoryMapper() {
    }

    static ShopPhotoCategory toDomain(ShopPhotoCategoryJpaEntity entity) {
        return ShopPhotoCategory.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getName()
        );
    }

    static ShopPhotoCategoryJpaEntity toEntity(ShopPhotoCategory domain) {
        return ShopPhotoCategoryJpaEntity.create(
            domain.getShopId(),
            domain.getName()
        );
    }

    static void applyChanges(ShopPhotoCategoryJpaEntity entity, ShopPhotoCategory domain) {
        entity.applyChanges(domain.getName());
    }
}
