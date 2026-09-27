package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourState;

final class ShopBusinessHourMapper {
    private ShopBusinessHourMapper() {
    }

    static ShopBusinessHourState toState(ShopBusinessHourJpaEntity entity) {
        return new ShopBusinessHourState(
            entity.getId(),
            entity.getShopId(),
            entity.getDayType(),
            entity.getOpenTime(),
            entity.getCloseTime(),
            entity.getIsClosed(),
            entity.getIs24Hours()
        );
    }

    static ShopBusinessHourJpaEntity toEntity(ShopBusinessHourState state) {
        return ShopBusinessHourJpaEntity.create(
            state.shopId(),
            state.dayType(),
            state.openTime(),
            state.closeTime(),
            state.isClosed(),
            state.is24Hours()
        );
    }

    static void applyChanges(ShopBusinessHourJpaEntity entity, ShopBusinessHourState state) {
        entity.applyChanges(
            state.dayType(),
            state.openTime(),
            state.closeTime(),
            state.isClosed(),
            state.is24Hours()
        );
    }
}
