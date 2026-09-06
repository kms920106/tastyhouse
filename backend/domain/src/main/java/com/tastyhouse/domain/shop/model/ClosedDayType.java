package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ClosedDayType {
    NO_CLOSED_DAYS("연중무휴", null, null),

    EVERY_WEEK_MONDAY("매주 월요일", DayOfWeek.MONDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_TUESDAY("매주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_WEDNESDAY("매주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_THURSDAY("매주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_FRIDAY("매주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_SATURDAY("매주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_SUNDAY("매주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.EVERY),

    EVERY_MONTH_FIRST_WEEK_MONDAY("매달 첫째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_TUESDAY("매달 첫째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_WEDNESDAY("매달 첫째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_THURSDAY("매달 첫째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_FRIDAY("매달 첫째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_SATURDAY("매달 첫째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_SUNDAY("매달 첫째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.FIRST),

    EVERY_MONTH_SECOND_WEEK_MONDAY("매달 둘째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_TUESDAY("매달 둘째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_WEDNESDAY("매달 둘째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_THURSDAY("매달 둘째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_FRIDAY("매달 둘째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_SATURDAY("매달 둘째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_SUNDAY("매달 둘째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.SECOND),

    EVERY_MONTH_THIRD_WEEK_MONDAY("매달 셋째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_TUESDAY("매달 셋째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_WEDNESDAY("매달 셋째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_THURSDAY("매달 셋째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_FRIDAY("매달 셋째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_SATURDAY("매달 셋째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_SUNDAY("매달 셋째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.THIRD),

    EVERY_MONTH_FOURTH_WEEK_MONDAY("매달 넷째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_TUESDAY("매달 넷째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_WEDNESDAY("매달 넷째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_THURSDAY("매달 넷째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_FRIDAY("매달 넷째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_SATURDAY("매달 넷째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_SUNDAY("매달 넷째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.FOURTH),

    EVERY_MONTH_LAST_WEEK_MONDAY("매달 마지막 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_TUESDAY("매달 마지막 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_WEDNESDAY("매달 마지막 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_THURSDAY("매달 마지막 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_FRIDAY("매달 마지막 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_SATURDAY("매달 마지막 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_SUNDAY("매달 마지막 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.LAST);

    private final String description;

    private final DayOfWeek dayOfWeek;

    private final WeekOrdinal weekOrdinal;

    ClosedDayType(String description, DayOfWeek dayOfWeek, WeekOrdinal weekOrdinal) {
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.weekOrdinal = weekOrdinal;
    }

    public String getDescription() {
        return this.description;
    }

    public DayOfWeek getDayOfWeek() {
        return this.dayOfWeek;
    }

    public WeekOrdinal getWeekOrdinal() {
        return this.weekOrdinal;
    }

    public boolean matches(LocalDate date) {
        if (dayOfWeek == null || date.getDayOfWeek() != dayOfWeek) {
            return false;
        }
        return weekOrdinal.matches(date);
    }

    public static ClosedDayType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.CLOSED_DAY_TYPE_UNKNOWN,
                ErrorCode.CLOSED_DAY_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public enum WeekOrdinal {
        EVERY {
            @Override
            boolean matches(LocalDate date) {
                return true;
            }
        },
        FIRST {
            @Override
            boolean matches(LocalDate date) {
                return weekOfMonth(date) == 1;
            }
        },
        SECOND {
            @Override
            boolean matches(LocalDate date) {
                return weekOfMonth(date) == 2;
            }
        },
        THIRD {
            @Override
            boolean matches(LocalDate date) {
                return weekOfMonth(date) == 3;
            }
        },
        FOURTH {
            @Override
            boolean matches(LocalDate date) {
                return weekOfMonth(date) == 4;
            }
        },
        LAST {
            @Override
            boolean matches(LocalDate date) {
                return date.plusWeeks(1).getMonthValue() != date.getMonthValue();
            }
        };

        abstract boolean matches(LocalDate date);

        private static int weekOfMonth(LocalDate date) {
            return ((date.getDayOfMonth() - 1) / 7) + 1;
        }
    }
}
