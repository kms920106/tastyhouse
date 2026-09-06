package com.tastyhouse.domain.shared.model;

import java.time.DayOfWeek;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum DayType {
    DAILY("매일", null),
    WEEKDAY("평일", null),
    WEEKEND("주말", null),
    HOLIDAY("공휴일", null),
    MONDAY("월요일", DayOfWeek.MONDAY),
    TUESDAY("화요일", DayOfWeek.TUESDAY),
    WEDNESDAY("수요일", DayOfWeek.WEDNESDAY),
    THURSDAY("목요일", DayOfWeek.THURSDAY),
    FRIDAY("금요일", DayOfWeek.FRIDAY),
    SATURDAY("토요일", DayOfWeek.SATURDAY),
    SUNDAY("일요일", DayOfWeek.SUNDAY);

    private final String description;

    private final DayOfWeek specificDayOfWeek;

    DayType(String description, DayOfWeek specificDayOfWeek) {
        this.description = description;
        this.specificDayOfWeek = specificDayOfWeek;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean appliesTo(DayOfWeek dayOfWeek, boolean publicHoliday) {
        return switch (this) {
            case DAILY -> true;
            case WEEKDAY -> !isWeekend(dayOfWeek);
            case WEEKEND -> isWeekend(dayOfWeek);
            case HOLIDAY -> publicHoliday;
            default -> isSpecificDay(dayOfWeek);
        };
    }

    public boolean isSpecificDay(DayOfWeek dayOfWeek) {
        return specificDayOfWeek != null && specificDayOfWeek == dayOfWeek;
    }

    private static boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static DayType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.DAY_TYPE_UNKNOWN,
                ErrorCode.DAY_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
