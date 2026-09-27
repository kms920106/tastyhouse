package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopClosedDayState;

final class ShopClosedDayStateMapper {
    private ShopClosedDayStateMapper() {
    }

    static ShopClosedDay toDomain(ShopClosedDayState state) {
        return ShopClosedDay.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.closedDayType() == null ? null : ClosedDayType.valueOf(state.closedDayType())
        );
    }

    static ShopClosedDayState toState(ShopClosedDay shopClosedDay) {
        return new ShopClosedDayState(
            shopClosedDay.getId(),
            shopClosedDay.getShopId() == null ? null : shopClosedDay.getShopId().value(),
            shopClosedDay.getClosedDayType() == null ? null : shopClosedDay.getClosedDayType().name()
        );
    }
}
