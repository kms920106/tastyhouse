package com.tastyhouse.domain.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public class ShopOperatingStatusCalculator {
    public ShopOperatingStatusResult calculate(ShopOperatingStatusContext context) {
        Shop shop = context.shop();
        LocalDateTime now = context.now();
        boolean publicHoliday = context.publicHoliday();

        if (shop.isPermanentlyClosed()) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.PERMANENTLY_CLOSED);
        }

        if (shop.isHidden()) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.HIDDEN);
        }

        if (hasActiveSuspension(context.suspensions(), now, context.orderMethod())) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.SUSPENDED);
        }

        if (publicHoliday && shop.isClosedOnPublicHolidays()) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.PUBLIC_HOLIDAY_CLOSED);
        }

        LocalDate today = now.toLocalDate();
        if (isTemporarilyClosed(context.temporaryClosures(), today)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.TEMPORARILY_CLOSED);
        }

        if (isRegularClosedDay(context.closedDays(), today)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.REGULAR_CLOSED_DAY);
        }

        if (!isWithinBusinessHours(context.businessHours(), now, publicHoliday)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.OUT_OF_BUSINESS_HOURS);
        }

        if (isWithinBreakTime(context.breakTimes(), now, publicHoliday)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.BREAK_TIME);
        }

        return ShopOperatingStatusResult.open();
    }

    private boolean hasActiveSuspension(List<ShopSuspension> suspensions, LocalDateTime now, OrderMethod orderMethod) {
        return suspensions.stream().anyMatch(suspension -> suspension.isActive(now, orderMethod));
    }

    private boolean isTemporarilyClosed(List<ShopTemporaryClosure> temporaryClosures, LocalDate today) {
        return temporaryClosures.stream()
            .anyMatch(closure -> !today.isBefore(closure.getStartDate()) && !today.isAfter(closure.getEndDate()));
    }

    private boolean isRegularClosedDay(List<ShopClosedDay> closedDays, LocalDate today) {
        return closedDays.stream()
            .anyMatch(closedDay -> closedDay.getClosedDayType().matches(today));
    }

    private boolean isWithinBusinessHours(List<ShopBusinessHour> businessHours, LocalDateTime now, boolean publicHoliday) {
        if (businessHours.isEmpty()) {
            return true;
        }

        LocalTime time = now.toLocalTime();

        ShopBusinessHour todayHour = selectApplicableHour(businessHours, now.getDayOfWeek(), publicHoliday);
        boolean openToday = todayHour != null && todayHour.isOpenAt(time);

        LocalDateTime yesterday = now.minusDays(1);
        ShopBusinessHour yesterdayHour = selectApplicableHour(businessHours, yesterday.getDayOfWeek(), false);
        boolean openFromYesterday = yesterdayHour != null && yesterdayHour.extendsIntoNextDayAt(time);

        return openToday || openFromYesterday;
    }

    private boolean isWithinBreakTime(List<ShopBreakTime> breakTimes, LocalDateTime now, boolean publicHoliday) {
        LocalTime time = now.toLocalTime();
        return breakTimes.stream()
            .anyMatch(breakTime -> breakTime.covers(time, now.getDayOfWeek(), publicHoliday));
    }

    public ShopBusinessHour selectApplicableHour(List<ShopBusinessHour> businessHours, DayOfWeek dayOfWeek, boolean publicHoliday) {
        ShopBusinessHour daily = null;
        ShopBusinessHour holiday = null;
        ShopBusinessHour weekGroup = null;
        for (ShopBusinessHour hour : businessHours) {
            DayType dayType = hour.getDayType();
            if (dayType.isSpecificDay(dayOfWeek)) {
                return hour;
            }
            if (dayType == DayType.HOLIDAY) {
                holiday = hour;
            } else if (dayType == DayType.WEEKEND && dayType.appliesTo(dayOfWeek, publicHoliday)) {
                weekGroup = hour;
            } else if (dayType == DayType.WEEKDAY && dayType.appliesTo(dayOfWeek, publicHoliday)) {
                weekGroup = hour;
            } else if (dayType == DayType.DAILY) {
                daily = hour;
            }
        }
        if (weekGroup != null) {
            return weekGroup;
        }
        if (publicHoliday && holiday != null) {
            return holiday;
        }
        return daily;
    }
}
