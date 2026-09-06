package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;

public record ShopNextOpenTimeContext(
    LocalDateTime now,
    List<ShopBusinessHour> businessHours,
    List<ShopClosedDay> closedDays,
    Set<LocalDate> publicHolidays
) {
    public ShopNextOpenTimeContext {
        businessHours = businessHours != null ? List.copyOf(businessHours) : List.of();
        closedDays = closedDays != null ? List.copyOf(closedDays) : List.of();
        publicHolidays = publicHolidays != null ? Set.copyOf(publicHolidays) : Set.of();
    }

    public static ShopNextOpenTimeContext of(
        LocalDateTime now,
        List<ShopBusinessHour> businessHours,
        List<ShopClosedDay> closedDays,
        Set<LocalDate> publicHolidays
    ) {
        return new ShopNextOpenTimeContext(
            now,
            businessHours,
            closedDays,
            publicHolidays
        );
    }
}
