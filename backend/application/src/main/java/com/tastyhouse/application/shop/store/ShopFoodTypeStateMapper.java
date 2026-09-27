package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeState;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopFoodTypeStateMapper {
    private ShopFoodTypeStateMapper() {
    }

    static ShopFoodType toDomain(ShopFoodTypeState state) {
        return ShopFoodType.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.shopFoodTypeCategoryId() == null ? null : ShopFoodTypeCategoryId.of(state.shopFoodTypeCategoryId())
        );
    }

    static ShopFoodTypeState toState(ShopFoodType shopFoodType) {
        return new ShopFoodTypeState(
            shopFoodType.getId(),
            shopFoodType.getShopId() == null ? null : shopFoodType.getShopId().value(),
            shopFoodType.getShopFoodTypeCategoryId() == null ? null : shopFoodType.getShopFoodTypeCategoryId().value()
        );
    }
}
