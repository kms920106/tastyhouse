package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopPhotoCategoryMapper {

    private ShopPhotoCategoryMapper() {
    }

    static ShopPhotoCategory toDomain(ShopPhotoCategoryJpaEntity entity) {
        return ShopPhotoCategory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getName()
        );
    }

    static ShopPhotoCategoryJpaEntity toEntity(ShopPhotoCategory domain) {
        return ShopPhotoCategoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getName()
        );
    }

    static void applyChanges(ShopPhotoCategoryJpaEntity entity, ShopPhotoCategory domain) {
        entity.applyChanges(domain.getName());
    }
}
