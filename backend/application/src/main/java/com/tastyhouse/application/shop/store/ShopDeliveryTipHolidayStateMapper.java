package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipHolidayState;

final class ShopDeliveryTipHolidayStateMapper {
    private ShopDeliveryTipHolidayStateMapper() {
    }

    static ShopDeliveryTipHoliday toDomain(ShopDeliveryTipHolidayState state) {
        return ShopDeliveryTipHoliday.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipHolidayState toState(ShopDeliveryTipHoliday shopDeliveryTipHoliday) {
        return new ShopDeliveryTipHolidayState(
            shopDeliveryTipHoliday.getId(),
            shopDeliveryTipHoliday.getShopId() == null ? null : shopDeliveryTipHoliday.getShopId().value(),
            shopDeliveryTipHoliday.getTipAmount()
        );
    }
}
