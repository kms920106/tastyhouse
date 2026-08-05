package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopBusinessHourMapper {

    private ShopBusinessHourMapper() {
    }

    static ShopBusinessHour toDomain(ShopBusinessHourJpaEntity entity) {
        return ShopBusinessHour.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getDayType(),
            entity.getOpenTime(),
            entity.getCloseTime(),
            entity.getIsClosed(),
            entity.getIs24Hours()
        );
    }

    static ShopBusinessHourJpaEntity toEntity(ShopBusinessHour domain) {
        return ShopBusinessHourJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
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
