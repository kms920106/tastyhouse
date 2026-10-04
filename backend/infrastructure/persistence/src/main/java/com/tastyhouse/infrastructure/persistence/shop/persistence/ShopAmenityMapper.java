package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopAmenityMapper {

    private ShopAmenityMapper() {
    }

    static ShopAmenity toDomain(ShopAmenityJpaEntity entity) {
        return ShopAmenity.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getShopAmenityCategoryId() == null ? null : ShopAmenityCategoryId.of(entity.getShopAmenityCategoryId())
        );
    }

    static ShopAmenityJpaEntity toEntity(ShopAmenity shopAmenity) {
        return ShopAmenityJpaEntity.create(
            shopAmenity.getShopId() == null ? null : shopAmenity.getShopId().value(),
            shopAmenity.getShopAmenityCategoryId() == null ? null : shopAmenity.getShopAmenityCategoryId().value()
        );
    }
}
