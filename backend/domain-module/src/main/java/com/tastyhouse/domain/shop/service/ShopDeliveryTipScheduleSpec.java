package com.tastyhouse.domain.shop.service;

import java.time.LocalTime;

import com.tastyhouse.domain.shop.model.DayType;

/**
 * 시간별 배달팁 replace-all 입력 한 행.
 */
public record ShopDeliveryTipScheduleSpec(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {

    public static ShopDeliveryTipScheduleSpec of(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        return new ShopDeliveryTipScheduleSpec(dayType, startTime, endTime, tipAmount);
    }
}
