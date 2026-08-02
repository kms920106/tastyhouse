package com.tastyhouse.domain.shop.domain.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.model.SuspensionReason;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;

/**
 * 영업 상태 계산기 순수 단위 테스트. Spring/JPA 컨텍스트 없이 우선순위·경계 케이스를 검증한다.
 */
class ShopOperatingStatusCalculatorTest {

    private final ShopOperatingStatusCalculator calculator = new ShopOperatingStatusCalculator();

    // 2026-07-27은 월요일
    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 7, 27, 12, 0);

    private Shop shop() {
        return Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            false, false, false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Shop hiddenShop() {
        return Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            false, true, false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("영업시간 내이고 휴무·중지 없으면 영업중")
    void open_withinBusinessHours() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), List.of(), List.of(), List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.OPEN);
    }

    @Test
    @DisplayName("영업시간 밖이면 준비중")
    void preparing_outsideBusinessHours() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), List.of(), List.of(), List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
    }

    @Test
    @DisplayName("24시간 영업이면 시간과 무관하게 영업중")
    void open_24hours() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, null, null, false, true)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), List.of(), List.of(), List.of(), false,
            LocalDateTime.of(2026, 7, 27, 3, 0)
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.OPEN);
    }

    @Test
    @DisplayName("전일 영업시간이 자정을 넘겨 새벽까지 이어지면 영업중")
    void open_crossMidnightFromYesterday() {
        // 매일 20:00 ~ 02:00 (자정 넘김) → 월요일 01:00은 일요일 영업의 연장
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(20, 0), LocalTime.of(2, 0), false, false)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), List.of(), List.of(), List.of(), false,
            LocalDateTime.of(2026, 7, 27, 1, 0)
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.OPEN);
    }

    @Test
    @DisplayName("활성 임시중지가 있으면 영업시간 내여도 준비중")
    void preparing_activeSuspension() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );
        List<ShopSuspension> suspensions = List.of(
            ShopSuspension.reconstitute(1L, ShopId.of(1L), SuspensionReason.SHOP_CIRCUMSTANCE, null,
                MONDAY_NOON.minusHours(1), MONDAY_NOON.plusHours(1), null, null, null)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), List.of(), List.of(), suspensions, false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
    }

    @Test
    @DisplayName("임시휴무 기간 내면 준비중")
    void preparing_temporaryClosure() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );
        List<ShopTemporaryClosure> closures = List.of(
            ShopTemporaryClosure.reconstitute(1L, ShopId.of(1L), LocalDate.of(2026, 7, 26), LocalDate.of(2026, 7, 28), null)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), List.of(), closures, List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
    }

    @Test
    @DisplayName("매주 월요일 정기휴무면 월요일에 준비중")
    void preparing_everyMondayClosed() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );
        List<ShopClosedDay> closedDays = List.of(
            ShopClosedDay.reconstitute(1L, ShopId.of(1L), ClosedDayType.EVERY_WEEK_MONDAY)
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, List.of(), closedDays, List.of(), List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
    }

    @Test
    @DisplayName("매달 넷째 주 월요일 정기휴무는 4째 주 월요일에만 준비중")
    void fourthWeekMonday() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );
        List<ShopClosedDay> closedDays = List.of(
            ShopClosedDay.reconstitute(1L, ShopId.of(1L), ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY)
        );

        // 2026-07-27은 넷째 주 월요일((27-1)/7+1 = 4) → 준비중
        assertThat(calculator.calculate(shop(), hours, List.of(), closedDays, List.of(), List.of(), false,
            LocalDateTime.of(2026, 7, 27, 12, 0))).isEqualTo(ShopOperatingStatus.PREPARING);

        // 2026-07-06은 첫째 주 월요일 → 영업중
        assertThat(calculator.calculate(shop(), hours, List.of(), closedDays, List.of(), List.of(), false,
            LocalDateTime.of(2026, 7, 6, 12, 0))).isEqualTo(ShopOperatingStatus.OPEN);
    }

    @Test
    @DisplayName("휴게시간 구간 내면 준비중")
    void preparing_withinBreakTime() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );
        List<ShopBreakTime> breakTimes = List.of(
            ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(11, 30), LocalTime.of(13, 0))
        );

        ShopOperatingStatus status = calculator.calculate(
            shop(), hours, breakTimes, List.of(), List.of(), List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
    }

    @Test
    @DisplayName("노출정지 가게는 방어적으로 준비중")
    void preparing_hiddenShop() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );

        ShopOperatingStatus status = calculator.calculate(
            hiddenShop(), hours, List.of(), List.of(), List.of(), List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
    }

    @Test
    @DisplayName("영업시간 정보가 없으면 영업중으로 간주")
    void open_whenNoBusinessHours() {
        ShopOperatingStatus status = calculator.calculate(
            shop(), List.of(), List.of(), List.of(), List.of(), List.of(), false, MONDAY_NOON
        );

        assertThat(status).isEqualTo(ShopOperatingStatus.OPEN);
    }
}
