package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopClosedDayMapper {

    private ShopClosedDayMapper() {
    }

    static ShopClosedDay toDomain(ShopClosedDayJpaEntity entity) {
        return ShopClosedDay.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getClosedDayType()
        );
    }

    static ShopClosedDayJpaEntity toEntity(ShopClosedDay domain) {
        return ShopClosedDayJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getClosedDayType()
        );
    }
}
