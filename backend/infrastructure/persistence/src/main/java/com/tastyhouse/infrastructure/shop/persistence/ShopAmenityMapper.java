package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopAmenityState;

final class ShopAmenityMapper {
    private ShopAmenityMapper() {
    }

    static ShopAmenityState toState(ShopAmenityJpaEntity entity) {
        return new ShopAmenityState(
            entity.getId(),
            entity.getShopId(),
            entity.getShopAmenityCategoryId()
        );
    }

    static ShopAmenityJpaEntity toEntity(ShopAmenityState state) {
        return ShopAmenityJpaEntity.create(
            state.shopId(),
            state.shopAmenityCategoryId()
        );
    }
}
