package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeState;

final class ShopFoodTypeMapper {
    private ShopFoodTypeMapper() {
    }

    static ShopFoodTypeState toState(ShopFoodTypeJpaEntity entity) {
        return new ShopFoodTypeState(
            entity.getId(),
            entity.getShopId(),
            entity.getShopFoodTypeCategoryId()
        );
    }

    static ShopFoodTypeJpaEntity toEntity(ShopFoodTypeState state) {
        return ShopFoodTypeJpaEntity.create(
            state.shopId(),
            state.shopFoodTypeCategoryId()
        );
    }
}
