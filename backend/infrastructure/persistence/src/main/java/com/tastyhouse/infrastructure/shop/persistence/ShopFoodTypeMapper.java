package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopFoodTypeMapper {
    private ShopFoodTypeMapper() {
    }

    static ShopFoodType toDomain(ShopFoodTypeJpaEntity entity) {
        return ShopFoodType.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getShopFoodTypeCategoryId() == null ? null : ShopFoodTypeCategoryId.of(entity.getShopFoodTypeCategoryId())
        );
    }

    static ShopFoodTypeJpaEntity toEntity(ShopFoodType shopFoodType) {
        return ShopFoodTypeJpaEntity.create(
            shopFoodType.getShopId() == null ? null : shopFoodType.getShopId().value(),
            shopFoodType.getShopFoodTypeCategoryId() == null ? null : shopFoodType.getShopFoodTypeCategoryId().value()
        );
    }
}
