package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopBreakTimeMapper {

    private ShopBreakTimeMapper() {
    }

    static ShopBreakTime toDomain(ShopBreakTimeJpaEntity entity) {
        return ShopBreakTime.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getDayType() == null ? null : DayType.valueOf(entity.getDayType()),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ShopBreakTimeJpaEntity toEntity(ShopBreakTime shopBreakTime) {
        return ShopBreakTimeJpaEntity.create(
            shopBreakTime.getShopId() == null ? null : shopBreakTime.getShopId().value(),
            shopBreakTime.getDayType() == null ? null : shopBreakTime.getDayType().name(),
            shopBreakTime.getStartTime(),
            shopBreakTime.getEndTime()
        );
    }

    static void applyChanges(ShopBreakTimeJpaEntity entity, ShopBreakTime shopBreakTime) {
        entity.applyChanges(
            shopBreakTime.getDayType() == null ? null : shopBreakTime.getDayType().name(),
            shopBreakTime.getStartTime(),
            shopBreakTime.getEndTime()
        );
    }
}
