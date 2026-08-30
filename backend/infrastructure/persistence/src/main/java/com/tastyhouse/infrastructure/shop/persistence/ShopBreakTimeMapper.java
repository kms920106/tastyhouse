package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopBreakTimeMapper {

    private ShopBreakTimeMapper() {
    }

    static ShopBreakTime toDomain(ShopBreakTimeJpaEntity entity) {
        return ShopBreakTime.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getDayType(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }

    static ShopBreakTimeJpaEntity toEntity(ShopBreakTime domain) {
        return ShopBreakTimeJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
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
