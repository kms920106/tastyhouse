package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public record ScheduledOrderSlotContext(
    Shop shop,
    OrderMethod orderMethod,
    LocalDateTime now,
    List<ShopBusinessHour> businessHours,
    List<ShopBreakTime> breakTimes,
    List<ShopClosedDay> closedDays,
    List<ShopTemporaryClosure> temporaryClosures,
    List<ShopSuspension> suspensions,
    List<ShopOrderMethod> shopOrderMethods
) {
    public ScheduledOrderSlotContext {
        businessHours = businessHours == null ? List.of() : List.copyOf(businessHours);
        breakTimes = breakTimes == null ? List.of() : List.copyOf(breakTimes);
        closedDays = closedDays == null ? List.of() : List.copyOf(closedDays);
        temporaryClosures = temporaryClosures == null ? List.of() : List.copyOf(temporaryClosures);
        suspensions = suspensions == null ? List.of() : List.copyOf(suspensions);
        shopOrderMethods = shopOrderMethods == null ? List.of() : List.copyOf(shopOrderMethods);
    }

    public static ScheduledOrderSlotContext of(
        Shop shop,
        OrderMethod orderMethod,
        LocalDateTime now,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        List<ShopOrderMethod> shopOrderMethods
    ) {
        return new ScheduledOrderSlotContext(
            shop,
            orderMethod,
            now,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            shopOrderMethods
        );
    }

    public boolean supportsOrderMethod() {
        return shopOrderMethods.stream()
            .anyMatch(assigned -> assigned.getOrderMethod() == orderMethod);
    }
}
