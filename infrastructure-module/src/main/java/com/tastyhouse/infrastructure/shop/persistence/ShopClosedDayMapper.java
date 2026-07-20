package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;

final class ShopClosedDayMapper {

    private ShopClosedDayMapper() {
    }

    static ShopClosedDay toDomain(ShopClosedDayJpaEntity entity) {
        return ShopClosedDay.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getClosedDayType()
        );
    }

    static ShopClosedDayJpaEntity toEntity(ShopClosedDay domain) {
        return ShopClosedDayJpaEntity.create(
            domain.getShopId(),
            domain.getClosedDayType()
        );
    }
}
