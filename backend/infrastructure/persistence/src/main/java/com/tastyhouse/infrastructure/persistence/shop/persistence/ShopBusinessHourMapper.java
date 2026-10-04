package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopBusinessHourMapper {

    private ShopBusinessHourMapper() {
    }

    static ShopBusinessHour toDomain(ShopBusinessHourJpaEntity entity) {
        return ShopBusinessHour.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getDayType() == null ? null : DayType.valueOf(entity.getDayType()),
            entity.getOpenTime(),
            entity.getCloseTime(),
            entity.getIsClosed(),
            entity.getIs24Hours()
        );
    }

    static ShopBusinessHourJpaEntity toEntity(ShopBusinessHour shopBusinessHour) {
        return ShopBusinessHourJpaEntity.create(
            shopBusinessHour.getShopId() == null ? null : shopBusinessHour.getShopId().value(),
            shopBusinessHour.getDayType() == null ? null : shopBusinessHour.getDayType().name(),
            shopBusinessHour.getOpenTime(),
            shopBusinessHour.getCloseTime(),
            shopBusinessHour.getIsClosed(),
            shopBusinessHour.getIs24Hours()
        );
    }

    static void applyChanges(ShopBusinessHourJpaEntity entity, ShopBusinessHour shopBusinessHour) {
        entity.applyChanges(
            shopBusinessHour.getDayType() == null ? null : shopBusinessHour.getDayType().name(),
            shopBusinessHour.getOpenTime(),
            shopBusinessHour.getCloseTime(),
            shopBusinessHour.getIsClosed(),
            shopBusinessHour.getIs24Hours()
        );
    }
}
