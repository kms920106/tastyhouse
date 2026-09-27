package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopBreakTimeState;

final class ShopBreakTimeMapper {
    private ShopBreakTimeMapper() {
    }

    static ShopBreakTimeState toState(ShopBreakTimeJpaEntity entity) {
        return new ShopBreakTimeState(
            entity.getId(),
            entity.getShopId(),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ShopBreakTimeJpaEntity toEntity(ShopBreakTimeState state) {
        return ShopBreakTimeJpaEntity.create(
            state.shopId(),
            state.dayType(),
            state.startTime(),
            state.endTime()
        );
    }

    static void applyChanges(ShopBreakTimeJpaEntity entity, ShopBreakTimeState state) {
        entity.applyChanges(
            state.dayType(),
            state.startTime(),
            state.endTime()
        );
    }
}
