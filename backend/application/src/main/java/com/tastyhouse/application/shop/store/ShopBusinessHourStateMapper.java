package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourState;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopBusinessHourStateMapper {
    private ShopBusinessHourStateMapper() {
    }

    static ShopBusinessHour toDomain(ShopBusinessHourState state) {
        return ShopBusinessHour.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.dayType() == null ? null : DayType.valueOf(state.dayType()),
            state.openTime(),
            state.closeTime(),
            state.isClosed(),
            state.is24Hours()
        );
    }

    static ShopBusinessHourState toState(ShopBusinessHour shopBusinessHour) {
        return new ShopBusinessHourState(
            shopBusinessHour.getId(),
            shopBusinessHour.getShopId() == null ? null : shopBusinessHour.getShopId().value(),
            shopBusinessHour.getDayType() == null ? null : shopBusinessHour.getDayType().name(),
            shopBusinessHour.getOpenTime(),
            shopBusinessHour.getCloseTime(),
            shopBusinessHour.getIsClosed(),
            shopBusinessHour.getIs24Hours()
        );
    }
}
