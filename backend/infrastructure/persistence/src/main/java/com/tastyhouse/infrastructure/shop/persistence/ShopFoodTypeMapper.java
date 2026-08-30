package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopFoodTypeMapper {

    private ShopFoodTypeMapper() {
    }

    static ShopFoodType toDomain(ShopFoodTypeJpaEntity entity) {
        return ShopFoodType.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getShopFoodTypeCategoryId(), ShopFoodTypeCategoryId::of)
        );
    }

    static ShopFoodTypeJpaEntity toEntity(ShopFoodType domain) {
        return ShopFoodTypeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getShopFoodTypeCategoryId(), ShopFoodTypeCategoryId::value)
        );
    }
}
