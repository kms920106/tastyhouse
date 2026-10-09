package com.tastyhouse.infrastructure.jpa.shop.persistence;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopClosedDayMapper {

    private ShopClosedDayMapper() {
    }

    static ShopClosedDay toDomain(ShopClosedDayJpaEntity entity) {
        return ShopClosedDay.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getClosedDayType() == null ? null : ClosedDayType.valueOf(entity.getClosedDayType())
        );
    }

    static ShopClosedDayJpaEntity toEntity(ShopClosedDay shopClosedDay) {
        return ShopClosedDayJpaEntity.create(
            shopClosedDay.getShopId() == null ? null : shopClosedDay.getShopId().value(),
            shopClosedDay.getClosedDayType() == null ? null : shopClosedDay.getClosedDayType().name()
        );
    }
}
