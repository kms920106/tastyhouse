package com.tastyhouse.domain.shop.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 정기 휴무 주기.
 *
 * <p>각 상수는 "어느 요일에({@code dayOfWeek}) 어느 주기로({@code weekOrdinal}) 쉬는가"를 <b>필드로</b>
 * 갖고, {@link #matches(LocalDate)}로 스스로 날짜 매칭을 판정한다. 과거에는 이 판정이
 * {@code ShopOperatingStatusCalculator}에서 상수 <b>이름 문자열</b>을 파싱해
 * ({@code name().endsWith("MONDAY")}, {@code name().startsWith("EVERY_WEEK_")}) 이뤄졌는데, 상수를
 * 리네이밍하면 컴파일은 통과하고 런타임 판정만 조용히 깨지는 구조였다.
 *
 * <p><b>상수 이름 자체는 DB 저장값이다</b>({@code EnumType.STRING}) — 필드를 추가하는 것은 DB에 영향이
 * 없지만 상수 이름은 절대 바꾸지 않는다.
 */
public enum ClosedDayType {

    // 연중무휴
    NO_CLOSED_DAYS("연중무휴", null, null),

    // 매주 특정 요일
    EVERY_WEEK_MONDAY("매주 월요일", DayOfWeek.MONDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_TUESDAY("매주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_WEDNESDAY("매주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_THURSDAY("매주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_FRIDAY("매주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_SATURDAY("매주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.EVERY),
    EVERY_WEEK_SUNDAY("매주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.EVERY),

    // 매달 첫째 주 특정 요일
    EVERY_MONTH_FIRST_WEEK_MONDAY("매달 첫째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_TUESDAY("매달 첫째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_WEDNESDAY("매달 첫째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_THURSDAY("매달 첫째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_FRIDAY("매달 첫째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_SATURDAY("매달 첫째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.FIRST),
    EVERY_MONTH_FIRST_WEEK_SUNDAY("매달 첫째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.FIRST),

    // 매달 둘째 주 특정 요일
    EVERY_MONTH_SECOND_WEEK_MONDAY("매달 둘째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_TUESDAY("매달 둘째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_WEDNESDAY("매달 둘째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_THURSDAY("매달 둘째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_FRIDAY("매달 둘째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_SATURDAY("매달 둘째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.SECOND),
    EVERY_MONTH_SECOND_WEEK_SUNDAY("매달 둘째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.SECOND),

    // 매달 셋째 주 특정 요일
    EVERY_MONTH_THIRD_WEEK_MONDAY("매달 셋째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_TUESDAY("매달 셋째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_WEDNESDAY("매달 셋째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_THURSDAY("매달 셋째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_FRIDAY("매달 셋째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_SATURDAY("매달 셋째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.THIRD),
    EVERY_MONTH_THIRD_WEEK_SUNDAY("매달 셋째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.THIRD),

    // 매달 넷째 주 특정 요일
    EVERY_MONTH_FOURTH_WEEK_MONDAY("매달 넷째 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_TUESDAY("매달 넷째 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_WEDNESDAY("매달 넷째 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_THURSDAY("매달 넷째 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_FRIDAY("매달 넷째 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_SATURDAY("매달 넷째 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.FOURTH),
    EVERY_MONTH_FOURTH_WEEK_SUNDAY("매달 넷째 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.FOURTH),

    // 매달 마지막 주 특정 요일
    EVERY_MONTH_LAST_WEEK_MONDAY("매달 마지막 주 월요일", DayOfWeek.MONDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_TUESDAY("매달 마지막 주 화요일", DayOfWeek.TUESDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_WEDNESDAY("매달 마지막 주 수요일", DayOfWeek.WEDNESDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_THURSDAY("매달 마지막 주 목요일", DayOfWeek.THURSDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_FRIDAY("매달 마지막 주 금요일", DayOfWeek.FRIDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_SATURDAY("매달 마지막 주 토요일", DayOfWeek.SATURDAY, WeekOrdinal.LAST),
    EVERY_MONTH_LAST_WEEK_SUNDAY("매달 마지막 주 일요일", DayOfWeek.SUNDAY, WeekOrdinal.LAST);

    private final String description;

    /** 휴무 요일. 연중무휴({@link #NO_CLOSED_DAYS})만 null이다. */
    private final DayOfWeek dayOfWeek;

    /** 휴무 주기(매주/첫째~넷째/마지막 주). 연중무휴만 null이다. */
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

    /**
     * 주어진 날짜가 이 정기휴무에 해당하는지 판정한다.
     *
     * <p>요일이 다르면 즉시 false다. 요일이 맞으면 주기를 확인한다 — 매주면 무조건 true, 첫째~넷째 주는
     * {@code (일자 - 1) / 7 + 1}로 계산한 주차와 비교하고, 마지막 주는 "1주 뒤가 다음 달인가"로 판정한다.
     * 연중무휴는 어떤 날짜에도 해당하지 않는다.
     */
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

    /**
     * 휴무 주기. 요일이 이미 일치한다는 전제에서 "그 요일 중 몇 번째인가"만 판정한다.
     */
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

        /** 같은 요일 기준 그 달의 몇 번째 주인지. 1일~7일이 1주차, 8일~14일이 2주차다. */
        private static int weekOfMonth(LocalDate date) {
            return ((date.getDayOfMonth() - 1) / 7) + 1;
        }
    }
}
