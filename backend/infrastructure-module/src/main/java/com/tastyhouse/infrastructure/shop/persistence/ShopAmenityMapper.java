package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopAmenityMapper {

    private ShopAmenityMapper() {
    }

    static ShopAmenity toDomain(ShopAmenityJpaEntity entity) {
        return ShopAmenity.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getShopAmenityCategoryId(), ShopAmenityCategoryId::of)
        );
    }

    static ShopAmenityJpaEntity toEntity(ShopAmenity domain) {
        return ShopAmenityJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getShopAmenityCategoryId(), ShopAmenityCategoryId::value)
        );
    }
}
