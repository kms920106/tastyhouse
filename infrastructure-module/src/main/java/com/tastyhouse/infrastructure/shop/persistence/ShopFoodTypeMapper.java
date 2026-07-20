package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopFoodType;

final class ShopFoodTypeMapper {

    private ShopFoodTypeMapper() {
    }

    static ShopFoodType toDomain(ShopFoodTypeJpaEntity entity) {
        return ShopFoodType.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getShopFoodTypeCategoryId()
        );
    }

    static ShopFoodTypeJpaEntity toEntity(ShopFoodType domain) {
        return ShopFoodTypeJpaEntity.create(
            domain.getShopId(),
            domain.getShopFoodTypeCategoryId()
        );
    }
}
