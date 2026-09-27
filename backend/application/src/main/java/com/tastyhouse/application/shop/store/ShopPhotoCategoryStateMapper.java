package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryState;

final class ShopPhotoCategoryStateMapper {
    private ShopPhotoCategoryStateMapper() {
    }

    static ShopPhotoCategory toDomain(ShopPhotoCategoryState state) {
        return ShopPhotoCategory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.name()
        );
    }

    static ShopPhotoCategoryState toState(ShopPhotoCategory shopPhotoCategory) {
        return new ShopPhotoCategoryState(
            shopPhotoCategory.getId(),
            shopPhotoCategory.getShopId() == null ? null : shopPhotoCategory.getShopId().value(),
            shopPhotoCategory.getName()
        );
    }
}
