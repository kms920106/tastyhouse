package com.tastyhouse.core.domain.place.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClosedDayType {

    // 연중무휴
    NO_CLOSED_DAYS("연중무휴"),

    // 매주 특정 요일
    EVERY_WEEK_MONDAY("매주 월요일"),
    EVERY_WEEK_TUESDAY("매주 화요일"),
    EVERY_WEEK_WEDNESDAY("매주 수요일"),
    EVERY_WEEK_THURSDAY("매주 목요일"),
    EVERY_WEEK_FRIDAY("매주 금요일"),
    EVERY_WEEK_SATURDAY("매주 토요일"),
    EVERY_WEEK_SUNDAY("매주 일요일"),

    // 매달 첫째 주 특정 요일
    EVERY_MONTH_FIRST_WEEK_MONDAY("매달 첫째 주 월요일"),
    EVERY_MONTH_FIRST_WEEK_TUESDAY("매달 첫째 주 화요일"),
    EVERY_MONTH_FIRST_WEEK_WEDNESDAY("매달 첫째 주 수요일"),
    EVERY_MONTH_FIRST_WEEK_THURSDAY("매달 첫째 주 목요일"),
    EVERY_MONTH_FIRST_WEEK_FRIDAY("매달 첫째 주 금요일"),
    EVERY_MONTH_FIRST_WEEK_SATURDAY("매달 첫째 주 토요일"),
    EVERY_MONTH_FIRST_WEEK_SUNDAY("매달 첫째 주 일요일"),

    // 매달 둘째 주 특정 요일
    EVERY_MONTH_SECOND_WEEK_MONDAY("매달 둘째 주 월요일"),
    EVERY_MONTH_SECOND_WEEK_TUESDAY("매달 둘째 주 화요일"),
    EVERY_MONTH_SECOND_WEEK_WEDNESDAY("매달 둘째 주 수요일"),
    EVERY_MONTH_SECOND_WEEK_THURSDAY("매달 둘째 주 목요일"),
    EVERY_MONTH_SECOND_WEEK_FRIDAY("매달 둘째 주 금요일"),
    EVERY_MONTH_SECOND_WEEK_SATURDAY("매달 둘째 주 토요일"),
    EVERY_MONTH_SECOND_WEEK_SUNDAY("매달 둘째 주 일요일"),

    // 매달 셋째 주 특정 요일
    EVERY_MONTH_THIRD_WEEK_MONDAY("매달 셋째 주 월요일"),
    EVERY_MONTH_THIRD_WEEK_TUESDAY("매달 셋째 주 화요일"),
    EVERY_MONTH_THIRD_WEEK_WEDNESDAY("매달 셋째 주 수요일"),
    EVERY_MONTH_THIRD_WEEK_THURSDAY("매달 셋째 주 목요일"),
    EVERY_MONTH_THIRD_WEEK_FRIDAY("매달 셋째 주 금요일"),
    EVERY_MONTH_THIRD_WEEK_SATURDAY("매달 셋째 주 토요일"),
    EVERY_MONTH_THIRD_WEEK_SUNDAY("매달 셋째 주 일요일"),

    // 매달 넷째 주 특정 요일
    EVERY_MONTH_FOURTH_WEEK_MONDAY("매달 넷째 주 월요일"),
    EVERY_MONTH_FOURTH_WEEK_TUESDAY("매달 넷째 주 화요일"),
    EVERY_MONTH_FOURTH_WEEK_WEDNESDAY("매달 넷째 주 수요일"),
    EVERY_MONTH_FOURTH_WEEK_THURSDAY("매달 넷째 주 목요일"),
    EVERY_MONTH_FOURTH_WEEK_FRIDAY("매달 넷째 주 금요일"),
    EVERY_MONTH_FOURTH_WEEK_SATURDAY("매달 넷째 주 토요일"),
    EVERY_MONTH_FOURTH_WEEK_SUNDAY("매달 넷째 주 일요일"),

    // 매달 마지막 주 특정 요일
    EVERY_MONTH_LAST_WEEK_MONDAY("매달 마지막 주 월요일"),
    EVERY_MONTH_LAST_WEEK_TUESDAY("매달 마지막 주 화요일"),
    EVERY_MONTH_LAST_WEEK_WEDNESDAY("매달 마지막 주 수요일"),
    EVERY_MONTH_LAST_WEEK_THURSDAY("매달 마지막 주 목요일"),
    EVERY_MONTH_LAST_WEEK_FRIDAY("매달 마지막 주 금요일"),
    EVERY_MONTH_LAST_WEEK_SATURDAY("매달 마지막 주 토요일"),
    EVERY_MONTH_LAST_WEEK_SUNDAY("매달 마지막 주 일요일");

    private final String description;
}
