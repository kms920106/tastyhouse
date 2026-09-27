package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipScheduleState;

final class ShopDeliveryTipScheduleStateMapper {
    private ShopDeliveryTipScheduleStateMapper() {
    }

    static ShopDeliveryTipSchedule toDomain(ShopDeliveryTipScheduleState state) {
        return ShopDeliveryTipSchedule.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.dayType() == null ? null : DayType.valueOf(state.dayType()),
            state.startTime(),
            state.endTime(),
            state.tipAmount()
        );
    }

    static ShopDeliveryTipScheduleState toState(ShopDeliveryTipSchedule shopDeliveryTipSchedule) {
        return new ShopDeliveryTipScheduleState(
            shopDeliveryTipSchedule.getId(),
            shopDeliveryTipSchedule.getShopId() == null ? null : shopDeliveryTipSchedule.getShopId().value(),
            shopDeliveryTipSchedule.getDayType() == null ? null : shopDeliveryTipSchedule.getDayType().name(),
            shopDeliveryTipSchedule.getStartTime(),
            shopDeliveryTipSchedule.getEndTime(),
            shopDeliveryTipSchedule.getTipAmount()
        );
    }
}
