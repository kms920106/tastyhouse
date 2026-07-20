package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;

final class ShopBusinessHourMapper {

    private ShopBusinessHourMapper() {
    }

    static ShopBusinessHour toDomain(ShopBusinessHourJpaEntity entity) {
        return ShopBusinessHour.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getDayType(),
            entity.getOpenTime(),
            entity.getCloseTime(),
            entity.getIsClosed()
        );
    }

    static ShopBusinessHourJpaEntity toEntity(ShopBusinessHour domain) {
        return ShopBusinessHourJpaEntity.create(
            domain.getShopId(),
            domain.getDayType(),
            domain.getOpenTime(),
            domain.getCloseTime(),
            domain.getIsClosed()
        );
    }

    static void applyChanges(ShopBusinessHourJpaEntity entity, ShopBusinessHour domain) {
        entity.applyChanges(
            domain.getDayType(),
            domain.getOpenTime(),
            domain.getCloseTime(),
            domain.getIsClosed()
        );
    }
}
