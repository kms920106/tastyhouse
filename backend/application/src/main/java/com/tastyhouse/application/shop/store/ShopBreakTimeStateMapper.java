package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopBreakTimeState;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopBreakTimeStateMapper {
    private ShopBreakTimeStateMapper() {
    }

    static ShopBreakTime toDomain(ShopBreakTimeState state) {
        return ShopBreakTime.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.dayType() == null ? null : DayType.valueOf(state.dayType()),
            state.startTime(),
            state.endTime()
        );
    }

    static ShopBreakTimeState toState(ShopBreakTime shopBreakTime) {
        return new ShopBreakTimeState(
            shopBreakTime.getId(),
            shopBreakTime.getShopId() == null ? null : shopBreakTime.getShopId().value(),
            shopBreakTime.getDayType() == null ? null : shopBreakTime.getDayType().name(),
            shopBreakTime.getStartTime(),
            shopBreakTime.getEndTime()
        );
    }
}
