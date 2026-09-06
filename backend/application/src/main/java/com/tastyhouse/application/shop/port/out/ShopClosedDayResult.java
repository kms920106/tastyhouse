package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.ClosedDayType;

public record ShopClosedDayResult(
    Long id,
    ClosedDayType closedDayType
) {
}
