package com.tastyhouse.core.domain.shop.application.dto.command;

import com.tastyhouse.core.domain.shop.domain.model.ClosedDayType;

public record ShopClosedDaySaveCommand(
    ClosedDayType closedDayType
) {

    public static ShopClosedDaySaveCommand of(ClosedDayType closedDayType) {
        return new ShopClosedDaySaveCommand(closedDayType);
    }
}
