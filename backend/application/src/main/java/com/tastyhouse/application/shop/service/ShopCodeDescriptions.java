package com.tastyhouse.application.shop.service;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ClosedDayType;

final class ShopCodeDescriptions {
    private ShopCodeDescriptions() {
    }

    static List<ShopOrderMethodResult> ofOrderMethods(List<ShopOrderMethodResult> orderMethods) {
        return orderMethods.stream()
            .map(orderMethod -> orderMethod.withOrderMethodDisplayName(orderMethod.orderMethod() == null
                ? null
                : OrderMethod.valueOf(orderMethod.orderMethod()).getDisplayName()))
            .toList();
    }

    static List<ShopBusinessHourResult> ofBusinessHours(List<ShopBusinessHourResult> businessHours) {
        return businessHours.stream()
            .map(businessHour -> businessHour.withDayTypeDescription(dayTypeDescription(businessHour.dayType())))
            .toList();
    }

    static List<ShopBreakTimeResult> ofBreakTimes(List<ShopBreakTimeResult> breakTimes) {
        return breakTimes.stream()
            .map(breakTime -> breakTime.withDayTypeDescription(dayTypeDescription(breakTime.dayType())))
            .toList();
    }

    static List<ShopClosedDayResult> ofClosedDays(List<ShopClosedDayResult> closedDays) {
        return closedDays.stream()
            .map(closedDay -> closedDay.withClosedDayTypeDescription(closedDay.closedDayType() == null
                ? null
                : ClosedDayType.valueOf(closedDay.closedDayType()).getDescription()))
            .toList();
    }

    private static String dayTypeDescription(String dayType) {
        return dayType == null ? null : DayType.valueOf(dayType).getDescription();
    }
}
