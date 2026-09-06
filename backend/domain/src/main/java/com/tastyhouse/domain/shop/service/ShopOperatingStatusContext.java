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

public record ShopOperatingStatusContext(
    Shop shop,
    List<ShopBusinessHour> businessHours,
    List<ShopBreakTime> breakTimes,
    List<ShopClosedDay> closedDays,
    List<ShopTemporaryClosure> temporaryClosures,
    List<ShopSuspension> suspensions,
    OrderMethod orderMethod,
    boolean publicHoliday,
    LocalDateTime now
) {
    public ShopOperatingStatusContext {
        businessHours = businessHours == null ? List.of() : List.copyOf(businessHours);
        breakTimes = breakTimes == null ? List.of() : List.copyOf(breakTimes);
        closedDays = closedDays == null ? List.of() : List.copyOf(closedDays);
        temporaryClosures = temporaryClosures == null ? List.of() : List.copyOf(temporaryClosures);
        suspensions = suspensions == null ? List.of() : List.copyOf(suspensions);
    }

    public static ShopOperatingStatusContext of(
        Shop shop,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        OrderMethod orderMethod,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        return new ShopOperatingStatusContext(
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
