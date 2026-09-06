package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;

public class ShopNextOpenTimeCalculator {
    private static final int SEARCH_DAYS = 7;

    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopNextOpenTimeCalculator(ShopOperatingStatusCalculator shopOperatingStatusCalculator) {
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    public LocalDateTime calculate(ShopNextOpenTimeContext context) {
        if (context.businessHours().isEmpty()) {
            return null;
        }

        LocalDate today = context.now().toLocalDate();
        for (int offset = 1; offset <= SEARCH_DAYS; offset++) {
            LocalDate candidate = today.plusDays(offset);

            if (isClosedDay(context, candidate)) {
                continue;
            }

            boolean publicHoliday = context.publicHolidays().contains(candidate);
            ShopBusinessHour hour = shopOperatingStatusCalculator.selectApplicableHour(
                context.businessHours(), candidate.getDayOfWeek(), publicHoliday);

            if (hour == null || hour.isClosed()) {
                continue;
            }

            if (hour.is24Hours() || hour.getOpenTime() == null) {
                continue;
            }
            return LocalDateTime.of(candidate, hour.getOpenTime());
        }
        return null;
    }

    private boolean isClosedDay(ShopNextOpenTimeContext context, LocalDate date) {
        for (ShopClosedDay closedDay : context.closedDays()) {
            if (closedDay.getClosedDayType().matches(date)) {
                return true;
            }
        }
        return false;
    }
}
