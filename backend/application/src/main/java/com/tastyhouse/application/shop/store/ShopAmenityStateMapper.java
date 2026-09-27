package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopAmenityState;

final class ShopAmenityStateMapper {
    private ShopAmenityStateMapper() {
    }

    static ShopAmenity toDomain(ShopAmenityState state) {
        return ShopAmenity.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.shopAmenityCategoryId() == null ? null : ShopAmenityCategoryId.of(state.shopAmenityCategoryId())
        );
    }

    static ShopAmenityState toState(ShopAmenity shopAmenity) {
        return new ShopAmenityState(
            shopAmenity.getId(),
            shopAmenity.getShopId() == null ? null : shopAmenity.getShopId().value(),
            shopAmenity.getShopAmenityCategoryId() == null ? null : shopAmenity.getShopAmenityCategoryId().value()
        );
    }
}
