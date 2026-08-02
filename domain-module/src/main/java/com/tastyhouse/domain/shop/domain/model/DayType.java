package com.tastyhouse.domain.shop.domain.model;

import java.time.DayOfWeek;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 영업시간·휴게시간이 적용되는 요일 구분.
 *
 * <p>개별 요일 상수는 대응하는 {@link DayOfWeek}를 필드로 갖고, {@link #appliesTo(DayOfWeek, boolean)}로
 * 스스로 적용 여부를 판정한다 — 이 매핑이 {@code ShopOperatingStatusCalculator}의 switch 표로 복제돼
 * 있으면 상수를 추가할 때 두 곳을 함께 고쳐야 한다.
 *
 * <p><b>상수 이름 자체는 DB 저장값이다</b>({@code EnumType.STRING}) — 이름을 바꾸지 않는다.
 */
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

    /** 개별 요일 상수만 값을 갖는다. 그룹 상수(매일/평일/주말/공휴일)는 null이다. */
    private final DayOfWeek specificDayOfWeek;

    DayType(String description, DayOfWeek specificDayOfWeek) {
        this.description = description;
        this.specificDayOfWeek = specificDayOfWeek;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * 이 요일 구분이 주어진 요일·공휴일 여부에 적용되는지 판정한다.
     */
    public boolean appliesTo(DayOfWeek dayOfWeek, boolean publicHoliday) {
        return switch (this) {
            case DAILY -> true;
            case WEEKDAY -> !isWeekend(dayOfWeek);
            case WEEKEND -> isWeekend(dayOfWeek);
            case HOLIDAY -> publicHoliday;
            default -> isSpecificDay(dayOfWeek);
        };
    }

    /** 이 상수가 주어진 요일을 콕 집어 가리키는 개별 요일 상수인지. */
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
