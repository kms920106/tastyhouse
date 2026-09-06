package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public record ShopOperatingStatusAggregates(
    List<ShopBusinessHour> businessHours,
    List<ShopBreakTime> breakTimes,
    List<ShopClosedDay> closedDays,
    List<ShopTemporaryClosure> temporaryClosures,
    List<ShopSuspension> suspensions
) {
    public static ShopOperatingStatusAggregates of(
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions
    ) {
        return new ShopOperatingStatusAggregates(
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions
        );
    }

    public ShopOperatingStatusContext toContext(
        Shop shop,
        OrderMethod orderMethod,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        return ShopOperatingStatusContext.of(
            shop,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            orderMethod,
            publicHoliday,
            now
        );
    }
}
