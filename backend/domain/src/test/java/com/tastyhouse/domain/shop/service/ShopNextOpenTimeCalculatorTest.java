package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopNextOpenTimeCalculatorTest {
    private static final ShopId SHOP_ID = ShopId.of(1L);

    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 8, 17, 12, 0);

    private final ShopNextOpenTimeCalculator calculator =
        new ShopNextOpenTimeCalculator(new ShopOperatingStatusCalculator());

    @Test
    @DisplayName("매일 영업이면 내일의 오픈 시각을 반환한다")
    void calculate_daily_returnsTomorrowOpenTime() {
        ShopNextOpenTimeContext context = context(
            List.of(businessHour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), Set.of());

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 18, 9, 0));
    }

    @Test
    @DisplayName("개별요일 영업시간이 매일 설정보다 우선한다")
    void calculate_specificDay_takesPrecedenceOverDaily() {
        ShopNextOpenTimeContext context = context(
            List.of(
                businessHour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0)),

                businessHour(DayType.TUESDAY, LocalTime.of(11, 0), LocalTime.of(20, 0))
            ),
            List.of(), Set.of());

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 18, 11, 0));
    }

    @Test
    @DisplayName("24시간 영업은 오픈 시각이 정의되지 않으므로 건너뛴다")
    void calculate_24Hours_isSkipped() {
        ShopNextOpenTimeContext context = context(
            List.of(

                businessHour(DayType.TUESDAY, null, null, false, true),
                businessHour(DayType.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(21, 0))
            ),
            List.of(), Set.of());

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 19, 10, 0));
    }

    @Test
    @DisplayName("정기휴무일은 건너뛰고 다음 영업일을 고른다")
    void calculate_closedDay_isSkipped() {
        ShopNextOpenTimeContext context = context(
            List.of(businessHour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0))),

            List.of(closedDay(ClosedDayType.EVERY_WEEK_TUESDAY)),
            Set.of());

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 19, 9, 0));
    }

    @Test
    @DisplayName("휴무 표시 행(isClosed)은 건너뛴다")
    void calculate_closedFlagRow_isSkipped() {
        ShopNextOpenTimeContext context = context(
            List.of(
                businessHour(DayType.TUESDAY, null, null, true, false),
                businessHour(DayType.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(21, 0))
            ),
            List.of(), Set.of());

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 19, 10, 30));
    }

    @Test
    @DisplayName("영업시간이 미등록이면 null을 반환한다(폴백은 호출부의 몫)")
    void calculate_noBusinessHours_returnsNull() {
        ShopNextOpenTimeContext context = context(List.of(), List.of(), Set.of());

        assertThat(calculator.calculate(context)).isNull();
    }

    @Test
    @DisplayName("+7일 내 영업일이 없으면 null을 반환한다")
    void calculate_noOpenDayWithinSevenDays_returnsNull() {
        ShopNextOpenTimeContext context = context(

            List.of(businessHour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(
                closedDay(ClosedDayType.EVERY_WEEK_MONDAY),
                closedDay(ClosedDayType.EVERY_WEEK_TUESDAY),
                closedDay(ClosedDayType.EVERY_WEEK_WEDNESDAY),
                closedDay(ClosedDayType.EVERY_WEEK_THURSDAY),
                closedDay(ClosedDayType.EVERY_WEEK_FRIDAY),
                closedDay(ClosedDayType.EVERY_WEEK_SATURDAY),
                closedDay(ClosedDayType.EVERY_WEEK_SUNDAY)
            ),
            Set.of());

        assertThat(calculator.calculate(context)).isNull();
    }

    @Test
    @DisplayName("오늘 남은 영업시간은 후보가 아니다 — 검사는 내일부터 시작한다")
    void calculate_doesNotPickTodayRemainingHours() {
        ShopNextOpenTimeContext context = context(
            List.of(businessHour(DayType.MONDAY, LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), Set.of());

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 24, 9, 0));
        assertThat(result.toLocalDate()).isNotEqualTo(MONDAY_NOON.toLocalDate());
    }

    @Test
    @DisplayName("공휴일에는 공휴일 영업시간 행이 적용된다")
    void calculate_publicHoliday_usesHolidayRow() {
        LocalDate tomorrow = LocalDate.of(2026, 8, 18);
        ShopNextOpenTimeContext context = context(
            List.of(
                businessHour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0)),
                businessHour(DayType.HOLIDAY, LocalTime.of(13, 0), LocalTime.of(20, 0))
            ),
            List.of(),
            Set.of(tomorrow));

        LocalDateTime result = calculator.calculate(context);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 8, 18, 13, 0));
    }

    private static ShopNextOpenTimeContext context(
        List<ShopBusinessHour> businessHours,
        List<ShopClosedDay> closedDays,
        Set<LocalDate> publicHolidays
    ) {
        return ShopNextOpenTimeContext.of(MONDAY_NOON, businessHours, closedDays, publicHolidays);
    }

    private static ShopBusinessHour businessHour(DayType dayType, LocalTime open, LocalTime close) {
        return businessHour(dayType, open, close, false, false);
    }

    private static ShopBusinessHour businessHour(
        DayType dayType,
        LocalTime open,
        LocalTime close,
        boolean closed,
        boolean allDay
    ) {
        return ShopBusinessHour.reconstitute(1L, SHOP_ID, dayType, open, close, closed, allDay);
    }

    private static ShopClosedDay closedDay(ClosedDayType closedDayType) {
        return ShopClosedDay.reconstitute(1L, SHOP_ID, closedDayType);
    }
}
