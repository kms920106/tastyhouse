package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.DayType;

public class ShopBreakTime {
    private final Long id;
    private final ShopId shopId;
    private DayType dayType;
    private LocalTime startTime;
    private LocalTime endTime;

    private ShopBreakTime(Long id, ShopId shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.shopId = shopId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ShopBreakTime of(ShopId shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        return new ShopBreakTime(null, shopId, dayType, startTime, endTime);
    }

    public static ShopBreakTime reconstitute(Long id, ShopId shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        return new ShopBreakTime(id, shopId, dayType, startTime, endTime);
    }

    public void update(DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean covers(LocalTime time, DayOfWeek dayOfWeek, boolean publicHoliday) {
        if (startTime == null || endTime == null) {
            return false;
        }
        if (!dayType.appliesTo(dayOfWeek, publicHoliday)) {
            return false;
        }
        if (endTime.isBefore(startTime)) {
            return !time.isBefore(startTime) || time.isBefore(endTime);
        }
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public DayType getDayType() {
        return this.dayType;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }
}
