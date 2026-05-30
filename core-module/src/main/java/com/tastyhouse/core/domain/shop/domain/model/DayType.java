package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayType {

    DAILY("매일"),
    WEEKDAY("평일"),
    WEEKEND("주말"),
    HOLIDAY("공휴일"),
    MONDAY("월요일"),
    TUESDAY("화요일"),
    WEDNESDAY("수요일"),
    THURSDAY("목요일"),
    FRIDAY("금요일"),
    SATURDAY("토요일"),
    SUNDAY("일요일");

    private final String description;
}
