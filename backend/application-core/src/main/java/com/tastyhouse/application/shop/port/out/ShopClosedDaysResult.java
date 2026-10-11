package com.tastyhouse.application.shop.port.out;

import java.util.List;

public record ShopClosedDaysResult(
    boolean closedOnPublicHolidays,
    List<ShopClosedDayResult> regularClosedDays,
    List<ShopTemporaryClosureResult> temporaryClosures
) {
}
