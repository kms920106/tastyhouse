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

/**
 * 노출 판정 <b>골든 테스트</b> — 계산기와 SQL 술어가 <b>반드시 같은 답을 내야 하는</b> 조합을
 * 결정표로 못박는다.
 *
 * <p><b>왜 필요한가</b>: 같은 규칙이 두 곳에 구현돼 있다 —
 * {@link ProductExposureCalculator}(상세·주문 검증)와
 * {@code ProductQueryDao#exposedNow}(목록 SQL 술어). 목록에 페이징이 걸려 있어 후처리로 걸러낼 수 없어
 * 술어가 불가피하고, 그래서 두 구현이 갈릴 위험이 구조적으로 존재한다. 갈리면 "목록에는 보이는데
 * 상세로 들어가면 주문이 거절되는" 형태로 조용히 드러난다.
 *
 * <p>이 표는 그 계약이다. <b>술어를 고칠 때 이 표를 함께 확인하고, 표를 바꿀 때는 양쪽을 함께 고친다.</b>
 * (DB 통합 테스트가 없는 저장소라 술어를 직접 실행해 대조할 수는 없다 — 표가 그 역할을 대신한다.)
 *
 * <p>기준일은 <b>2026-08-17(월)</b>이다. {@code HOLIDAY}는 표에서 제외한다 — SQL 술어가 공휴일 판정을
 * 갖지 않아 의도적으로 두 구현이 다르며, 그 차이는 {@code exposedNow} Javadoc에 명시돼 있다.
 */
class ProductExposureCalculatorGoldenTest {

    private static final ProductId PRODUCT_ID = ProductId.of(1L);
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 17);

    private final ProductExposureCalculator calculator = new ProductExposureCalculator();

    /**
     * @param dayType  설정한 요일 구분 (NONE이면 시간대 행 자체가 없음 = 제약 없음)
     * @param start    시작 시각 (EMPTY면 종일)
     * @param end      종료 시각 (EMPTY면 종일)
     * @param at       판정 시각 (기준일로부터의 오프셋 포함 — "D+1 01:00" 형태)
     * @param expected 노출 여부
     */
    @ParameterizedTest(name = "[{index}] {0} {1}~{2} @ {3} → 노출={4}")
    @CsvSource({
        // ── 시간대 행 0건 = 제약 없음 ─────────────────────────────────────────────
        "NONE,      EMPTY, EMPTY, D+0 03:00, true",
        "NONE,      EMPTY, EMPTY, D+0 12:00, true",

        // ── 종일(시작·종료 NULL) ──────────────────────────────────────────────────
        "MONDAY,    EMPTY, EMPTY, D+0 00:00, true",
        "MONDAY,    EMPTY, EMPTY, D+0 23:59, true",
        "MONDAY,    EMPTY, EMPTY, D+1 12:00, false",

        // ── 같은 날 구간: 시작 포함, 종료 배타 ────────────────────────────────────
        "MONDAY,    11:00, 14:00, D+0 10:59, false",
        "MONDAY,    11:00, 14:00, D+0 11:00, true",
        "MONDAY,    11:00, 14:00, D+0 13:59, true",
        "MONDAY,    11:00, 14:00, D+0 14:00, false",
        "MONDAY,    11:00, 14:00, D+0 15:00, false",

        // ── 요일 묶음 ────────────────────────────────────────────────────────────
        "DAILY,     11:00, 14:00, D+0 12:00, true",
        "WEEKDAY,   11:00, 14:00, D+0 12:00, true",
        "WEEKEND,   11:00, 14:00, D+0 12:00, false",
        "WEEKDAY,   11:00, 14:00, D+5 12:00, false",
        "WEEKEND,   11:00, 14:00, D+5 12:00, true",

        // ── 자정 넘김(야식) — 가장 틀리기 쉬운 지점 ──────────────────────────────
        "MONDAY,    22:00, 02:00, D+0 21:59, false",
        "MONDAY,    22:00, 02:00, D+0 22:00, true",
        "MONDAY,    22:00, 02:00, D+0 23:59, true",
        "MONDAY,    22:00, 02:00, D+1 00:30, true",
        "MONDAY,    22:00, 02:00, D+1 01:59, true",
        "MONDAY,    22:00, 02:00, D+1 02:00, false",
        "MONDAY,    22:00, 02:00, D+1 03:00, false",
        "MONDAY,    22:00, 02:00, D+0 12:00, false",

        // 자정 넘김 + DAILY: 매일 22시부터 다음날 2시까지 → 어느 날 새벽 1시도 노출
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

    /**
     * 기간 축 결정표. 종료일은 <b>당일을 포함</b>한다 — 술어의 {@code goe(today)}와 계산기의
     * {@code isAfter(endDate)}가 이 경계에서 일치해야 한다.
     */
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

    /** {@code "D+1 01:00"} 형태를 기준일 기준 시각으로 바꾼다. */
    private LocalDateTime parseAt(String value) {
        String[] parts = value.trim().split("\\s+");
        int dayOffset = Integer.parseInt(parts[0].substring(2));
        return MONDAY.plusDays(dayOffset).atTime(LocalTime.parse(parts[1]));
    }
}
