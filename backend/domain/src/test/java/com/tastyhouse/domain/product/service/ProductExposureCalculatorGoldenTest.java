package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

import static org.assertj.core.api.Assertions.assertThat;

class ProductExposureCalculatorGoldenTest {
    private static final ProductId PRODUCT_ID = ProductId.of(1L);
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 17);

    private final ProductExposureCalculator calculator = new ProductExposureCalculator();

    @ParameterizedTest(name = "[{index}] {0} {1}~{2} @ {3} → 노출={4}")
    @CsvSource({
        "NONE,      EMPTY, EMPTY, D+0 03:00, true",
        "NONE,      EMPTY, EMPTY, D+0 12:00, true",

        "MONDAY,    EMPTY, EMPTY, D+0 00:00, true",
        "MONDAY,    EMPTY, EMPTY, D+0 23:59, true",
        "MONDAY,    EMPTY, EMPTY, D+1 12:00, false",

        "MONDAY,    11:00, 14:00, D+0 10:59, false",
        "MONDAY,    11:00, 14:00, D+0 11:00, true",
        "MONDAY,    11:00, 14:00, D+0 13:59, true",
        "MONDAY,    11:00, 14:00, D+0 14:00, false",
        "MONDAY,    11:00, 14:00, D+0 15:00, false",

        "DAILY,     11:00, 14:00, D+0 12:00, true",
        "WEEKDAY,   11:00, 14:00, D+0 12:00, true",
        "WEEKEND,   11:00, 14:00, D+0 12:00, false",
        "WEEKDAY,   11:00, 14:00, D+5 12:00, false",
        "WEEKEND,   11:00, 14:00, D+5 12:00, true",

        "MONDAY,    22:00, 02:00, D+0 21:59, false",
        "MONDAY,    22:00, 02:00, D+0 22:00, true",
        "MONDAY,    22:00, 02:00, D+0 23:59, true",
        "MONDAY,    22:00, 02:00, D+1 00:30, true",
        "MONDAY,    22:00, 02:00, D+1 01:59, true",
        "MONDAY,    22:00, 02:00, D+1 02:00, false",
        "MONDAY,    22:00, 02:00, D+1 03:00, false",
        "MONDAY,    22:00, 02:00, D+0 12:00, false",

        "DAILY,     22:00, 02:00, D+3 01:00, true",
        "DAILY,     22:00, 02:00, D+3 12:00, false",
    })
    @DisplayName("계산기와 SQL 술어가 같은 답을 내야 하는 조합 결정표")
    void goldenTable(String dayType, String start, String end, String at, boolean expected) {
        List<ProductExposureHour> hours = "NONE".equals(dayType)
            ? List.of()
            : List.of(ProductExposureHour.of(
                PRODUCT_ID, DayType.valueOf(dayType), parseTime(start), parseTime(end)));

        ProductExposureResult result = calculator.calculate(ProductExposureContext.of(
            true, null, null, hours, parseAt(at), false, false));

        assertThat(result.exposed()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] 기간 {0}~{1} @ {2} → 노출={3}")
    @CsvSource({
        "EMPTY,      EMPTY,      D+0, true",
        "2026-08-17, EMPTY,      D+0, true",
        "2026-08-18, EMPTY,      D+0, false",
        "EMPTY,      2026-08-17, D+0, true",
        "EMPTY,      2026-08-16, D+0, false",
        "2026-08-17, 2026-08-17, D+0, true",
        "2026-08-16, 2026-08-18, D+0, true",
        "2026-08-16, 2026-08-18, D+3, false",
    })
    @DisplayName("기간 축 결정표 — 종료일 당일 포함")
    void goldenPeriodTable(String startDate, String endDate, String at, boolean expected) {
        ProductExposureResult result = calculator.calculate(ProductExposureContext.of(
            true,
            parseDate(startDate),
            parseDate(endDate),
            List.of(),
            parseAt(at + " 12:00"),
            false,
            false
        ));

        assertThat(result.exposed()).isEqualTo(expected);
    }

    private LocalTime parseTime(String value) {
        return "EMPTY".equals(value) ? null : LocalTime.parse(value);
    }

    private LocalDate parseDate(String value) {
        return "EMPTY".equals(value) ? null : LocalDate.parse(value);
    }

    private LocalDateTime parseAt(String value) {
        String[] parts = value.trim().split("\\s+");
        int dayOffset = Integer.parseInt(parts[0].substring(2));
        return MONDAY.plusDays(dayOffset).atTime(LocalTime.parse(parts[1]));
    }
}
