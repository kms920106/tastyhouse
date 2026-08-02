package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopAmenity;

final class ShopAmenityMapper {

    private ShopAmenityMapper() {
    }

    static ShopAmenity toDomain(ShopAmenityJpaEntity entity) {
        return ShopAmenity.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getShopAmenityCategoryId()
        );
    }

    static ShopAmenityJpaEntity toEntity(ShopAmenity domain) {
        return ShopAmenityJpaEntity.create(
            domain.getShopId(),
            domain.getShopAmenityCategoryId()
        );
    }
}
