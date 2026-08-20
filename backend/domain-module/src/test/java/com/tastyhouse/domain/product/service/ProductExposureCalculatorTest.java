package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.model.ProductHiddenReason;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 노출 판정 계산기의 순수 단위 테스트. Spring/DB/시계 없이 Context만으로 검증한다.
 *
 * <p>기준 시각은 <b>2026-08-17(월)</b>이다 — 요일 묶음(WEEKDAY)과 개별 요일(MONDAY)이 모두
 * 걸리는 날이라 우선순위·자정 넘김을 한 픽스처로 검증할 수 있다.
 */
class ProductExposureCalculatorTest {

    private static final ProductId PRODUCT_ID = ProductId.of(1L);
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 17);

    private final ProductExposureCalculator calculator = new ProductExposureCalculator();

    // ── 우선순위 ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("점주가 숨긴 메뉴는 스케줄과 무관하게 숨김이다 — 명시적 의사가 스케줄을 이긴다")
    void manuallyHidden_beatsSchedule() {
        // 지금이 노출 시간대 한복판이어도 visible=false면 숨김이다.
        ProductExposureResult result = calculator.calculate(context(
            false, null, null,
            List.of(hour(DayType.DAILY, LocalTime.of(0, 0), LocalTime.of(23, 59))),
            MONDAY.atTime(12, 0)
        ));

        assertThat(result.exposed()).isFalse();
        assertThat(result.hiddenReason()).isEqualTo(ProductHiddenReason.MANUALLY_HIDDEN);
    }

    @Test
    @DisplayName("노출 시작일 이전이면 숨김이다")
    void beforePeriod_hidden() {
        ProductExposureResult result = calculator.calculate(context(
            true, MONDAY.plusDays(1), null, List.of(), MONDAY.atTime(12, 0)
        ));

        assertThat(result.exposed()).isFalse();
        assertThat(result.hiddenReason()).isEqualTo(ProductHiddenReason.BEFORE_EXPOSURE_PERIOD);
    }

    @Test
    @DisplayName("노출 종료일 당일은 포함한다 — 그날 자정까지 노출된다")
    void endDate_isInclusive() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, MONDAY, List.of(), MONDAY.atTime(23, 59)
        ));

        assertThat(result.exposed()).isTrue();
    }

    @Test
    @DisplayName("노출 종료일 다음 날부터 숨김이다")
    void afterPeriod_hidden() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, MONDAY.minusDays(1), List.of(), MONDAY.atTime(0, 1)
        ));

        assertThat(result.exposed()).isFalse();
        assertThat(result.hiddenReason()).isEqualTo(ProductHiddenReason.AFTER_EXPOSURE_PERIOD);
    }

    // ── 행 0건 = 제약 없음 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("시간대 행이 0건이면 제약 없음이다 — 기존 메뉴 백필이 불필요한 근거")
    void noHours_meansNoConstraint() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null, List.of(), MONDAY.atTime(3, 0)
        ));

        assertThat(result.exposed()).isTrue();
        assertThat(result.hiddenReason()).isNull();
    }

    // ── 요일·시간대 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("설정된 시간대 안이면 노출된다")
    void withinHours_exposed() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))),
            MONDAY.atTime(12, 0)
        ));

        assertThat(result.exposed()).isTrue();
    }

    @Test
    @DisplayName("설정된 시간대 밖이면 숨김이다")
    void outsideHours_hidden() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))),
            MONDAY.atTime(15, 0)
        ));

        assertThat(result.exposed()).isFalse();
        assertThat(result.hiddenReason()).isEqualTo(ProductHiddenReason.OUT_OF_EXPOSURE_HOURS);
    }

    @Test
    @DisplayName("종료 시각은 배타적이다 — 14:00 설정이면 14:00 정각에는 숨김이다")
    void endTime_isExclusive() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))),
            MONDAY.atTime(14, 0)
        ));

        assertThat(result.exposed()).isFalse();
    }

    @Test
    @DisplayName("요일 묶음(WEEKDAY)은 평일에 적용된다")
    void weekdayGroup_appliesOnWeekday() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.WEEKDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))),
            MONDAY.atTime(12, 0)
        ));

        assertThat(result.exposed()).isTrue();
    }

    @Test
    @DisplayName("시작·종료가 모두 null이면 그 요일 종일 노출이다")
    void allDay_exposedWholeDay() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, null, null)),
            MONDAY.atTime(4, 30)
        ));

        assertThat(result.exposed()).isTrue();
    }

    // ── 자정 넘김(야식) — 이 계산기에서 가장 틀리기 쉬운 지점 ────────────────────────

    @Test
    @DisplayName("자정 넘김 22:00~02:00 — 당일 23:00은 노출된다")
    void overnight_lateEveningExposed() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(22, 0), LocalTime.of(2, 0))),
            MONDAY.atTime(23, 0)
        ));

        assertThat(result.exposed()).isTrue();
    }

    @Test
    @DisplayName("★ 자정 넘김 22:00~02:00 — 다음 날 01:00은 전일 행의 연장으로 노출된다")
    void overnight_earlyMorningExposedViaPreviousDay() {
        // 월요일 22:00~02:00 설정. 화요일 01:00은 '화요일 행'에는 걸리지 않고
        // '월요일 행이 자정을 넘어온 꼬리'에만 해당한다.
        // 전일 확인을 빠뜨리면 여기서 야식 메뉴가 사라진다.
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(22, 0), LocalTime.of(2, 0))),
            MONDAY.plusDays(1).atTime(1, 0)
        ));

        assertThat(result.exposed()).isTrue();
    }

    @Test
    @DisplayName("자정 넘김 22:00~02:00 — 다음 날 03:00은 꼬리 밖이라 숨김이다")
    void overnight_afterTailHidden() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(22, 0), LocalTime.of(2, 0))),
            MONDAY.plusDays(1).atTime(3, 0)
        ));

        assertThat(result.exposed()).isFalse();
        assertThat(result.hiddenReason()).isEqualTo(ProductHiddenReason.OUT_OF_EXPOSURE_HOURS);
    }

    @Test
    @DisplayName("자정 넘김 22:00~02:00 — 당일 12:00은 시작 전이라 숨김이다")
    void overnight_middayHidden() {
        ProductExposureResult result = calculator.calculate(context(
            true, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(22, 0), LocalTime.of(2, 0))),
            MONDAY.atTime(12, 0)
        ));

        assertThat(result.exposed()).isFalse();
    }

    // ── 기간과 시간대의 조합 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("기간은 시간대보다 먼저 판정된다 — 기간 밖이면 시간대가 맞아도 기간 사유로 숨김이다")
    void periodCheckedBeforeHours() {
        ProductExposureResult result = calculator.calculate(context(
            true, MONDAY.plusDays(3), null,
            List.of(hour(DayType.DAILY, LocalTime.of(0, 0), LocalTime.of(23, 59))),
            MONDAY.atTime(12, 0)
        ));

        assertThat(result.exposed()).isFalse();
        assertThat(result.hiddenReason()).isEqualTo(ProductHiddenReason.BEFORE_EXPOSURE_PERIOD);
    }

    // ── 픽스처 ─────────────────────────────────────────────────────────────────────

    private ProductExposureContext context(
        boolean visible,
        LocalDate startDate,
        LocalDate endDate,
        List<ProductExposureHour> hours,
        LocalDateTime now
    ) {
        return ProductExposureContext.of(visible, startDate, endDate, hours, now, false, false);
    }

    private ProductExposureHour hour(DayType dayType, LocalTime start, LocalTime end) {
        return ProductExposureHour.of(PRODUCT_ID, dayType, start, end);
    }
}
