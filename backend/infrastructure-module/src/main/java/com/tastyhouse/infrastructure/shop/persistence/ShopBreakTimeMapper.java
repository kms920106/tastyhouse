package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopBreakTime;

final class ShopBreakTimeMapper {

    private ShopBreakTimeMapper() {
    }

    static ShopBreakTime toDomain(ShopBreakTimeJpaEntity entity) {
        return ShopBreakTime.reconstitute(
            entity.getId(),
            entity.getShopId(),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ShopBreakTimeJpaEntity toEntity(ShopBreakTime domain) {
        return ShopBreakTimeJpaEntity.create(
            domain.getShopId(),
            domain.getDayType(),
            domain.getStartTime(),
            domain.getEndTime()
        );
    }

    static void applyChanges(ShopBreakTimeJpaEntity entity, ShopBreakTime domain) {
        entity.applyChanges(
            domain.getDayType(),
            domain.getStartTime(),
            domain.getEndTime()
        );
    }
}
