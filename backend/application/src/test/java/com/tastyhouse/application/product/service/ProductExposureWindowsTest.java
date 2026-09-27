package com.tastyhouse.application.product.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.tastyhouse.application.product.port.out.ProductExposureWindow;

import static org.assertj.core.api.Assertions.assertThat;

class ProductExposureWindowsTest {

    @ParameterizedTest
    @EnumSource(DayOfWeek.class)
    @DisplayName("오늘·전날 요일 유형은 DAILY + WEEKDAY/WEEKEND + 요일명이며 HOLIDAY는 포함하지 않는다")
    void dayTypesMatchPreviousDaoRule(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.of(2026, 9, 1).with(TemporalAdjusters.nextOrSame(dayOfWeek));
        LocalDateTime now = LocalDateTime.of(date, LocalTime.of(13, 0));

        ProductExposureWindow window = ProductExposureWindows.at(now);

        assertThat(window.now()).isEqualTo(now);
        assertThat(window.todayDayTypes()).isEqualTo(expectedDayTypes(dayOfWeek));
        assertThat(window.previousDayDayTypes()).isEqualTo(expectedDayTypes(dayOfWeek.minus(1)));
    }

    private static Set<String> expectedDayTypes(DayOfWeek dayOfWeek) {
        boolean weekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        return Set.of("DAILY", weekend ? "WEEKEND" : "WEEKDAY", dayOfWeek.name());
    }
}
