package com.tastyhouse.domain.shop.model;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.DayType;

public class ShopBusinessHour {
    private static final long MIN_DURATION_MINUTES = 60;

    private static final long MAX_DURATION_MINUTES = 23 * 60 + 55;

    private final Long id;
    private final ShopId shopId;
    private DayType dayType;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
    private Boolean is24Hours;

    private ShopBusinessHour(
        Long id,
        ShopId shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        this.id = id;
        this.shopId = shopId;
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    public static ShopBusinessHour of(
        ShopId shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        validateBusinessHour(openTime, closeTime, isClosed, is24Hours);

        return new ShopBusinessHour(null, shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    public static ShopBusinessHour reconstitute(
        Long id,
        ShopId shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHour(id, shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    public void update(DayType dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        validateBusinessHour(openTime, closeTime, isClosed, is24Hours);

        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    public boolean isOpenAt(LocalTime time) {
        if (is24Hours()) {
            return true;
        }
        if (isClosed() || openTime == null || closeTime == null) {
            return false;
        }
        return isWithinRange(time, openTime, closeTime);
    }

    public boolean extendsIntoNextDayAt(LocalTime time) {
        if (is24Hours() || isClosed() || openTime == null || closeTime == null) {
            return false;
        }
        return crossesMidnight(openTime, closeTime) && time.isBefore(closeTime);
    }

    public boolean isClosed() {
        return Boolean.TRUE.equals(isClosed);
    }

    public boolean is24Hours() {
        return Boolean.TRUE.equals(is24Hours);
    }

    public Boolean getIsClosed() {
        return this.isClosed;
    }

    public Boolean getIs24Hours() {
        return this.is24Hours;
    }

    private static boolean isWithinRange(LocalTime time, LocalTime start, LocalTime end) {
        if (crossesMidnight(start, end)) {
            return !time.isBefore(start) || time.isBefore(end);
        }
        return !time.isBefore(start) && time.isBefore(end);
    }

    private static boolean crossesMidnight(LocalTime start, LocalTime end) {
        return end.isBefore(start);
    }

    private static void validateBusinessHour(LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        if (Boolean.TRUE.equals(isClosed) || Boolean.TRUE.equals(is24Hours)) {
            return;
        }
        if (openTime == null || closeTime == null) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
        if (isNotFiveMinuteUnit(openTime) || isNotFiveMinuteUnit(closeTime)) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);
        }
        long durationMinutes = minutesBetween(openTime, closeTime);
        if (durationMinutes < MIN_DURATION_MINUTES || durationMinutes > MAX_DURATION_MINUTES) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
    }

    private static boolean isNotFiveMinuteUnit(LocalTime time) {
        return time.getMinute() % 5 != 0 || time.getSecond() != 0 || time.getNano() != 0;
    }

    private static long minutesBetween(LocalTime open, LocalTime close) {
        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        int diff = closeMin - openMin;
        if (diff <= 0) {
            diff += 24 * 60;
        }
        return diff;
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

    public LocalTime getOpenTime() {
        return this.openTime;
    }

    public LocalTime getCloseTime() {
        return this.closeTime;
    }
}
