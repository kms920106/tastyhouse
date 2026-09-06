package com.tastyhouse.domain.shop.service;

import java.time.LocalTime;

import com.tastyhouse.domain.shared.model.DayType;

public record ShopDeliveryTipScheduleSpec(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
    public static ShopDeliveryTipScheduleSpec of(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        return new ShopDeliveryTipScheduleSpec(dayType, startTime, endTime, tipAmount);
    }
}
