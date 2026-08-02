package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopBusinessHour;

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
            entity.getIsClosed(),
            entity.getIs24Hours()
        );
    }

    static ShopBusinessHourJpaEntity toEntity(ShopBusinessHour domain) {
        return ShopBusinessHourJpaEntity.create(
            domain.getShopId(),
            domain.getDayType(),
            domain.getOpenTime(),
            domain.getCloseTime(),
            domain.getIsClosed(),
            domain.getIs24Hours()
        );
    }

    static void applyChanges(ShopBusinessHourJpaEntity entity, ShopBusinessHour domain) {
        entity.applyChanges(
            domain.getDayType(),
            domain.getOpenTime(),
            domain.getCloseTime(),
            domain.getIsClosed(),
            domain.getIs24Hours()
        );
    }
}
