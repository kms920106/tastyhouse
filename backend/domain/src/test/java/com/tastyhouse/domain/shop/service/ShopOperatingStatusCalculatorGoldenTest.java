package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;

class ShopOperatingStatusCalculatorGoldenTest {
    private final ShopOperatingStatusCalculator calculator = new ShopOperatingStatusCalculator();

    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 27);

    private Shop shop() {
        return shop(false, false);
    }

    private Shop shop(boolean permanentlyClosed, boolean closedOnPublicHolidays) {
        return Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            permanentlyClosed, false, closedOnPublicHolidays, 0, false, false, false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ShopOperatingStatus statusAt(
        List<ShopBusinessHour> hours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        return statusAt(shop(), hours, breakTimes, closedDays, publicHoliday, now);
    }

    private ShopOperatingStatus statusAt(
        Shop shop,
        List<ShopBusinessHour> hours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        return calculator.calculate(ShopOperatingStatusContext.of(
            shop, hours, breakTimes, closedDays, List.of(), List.of(), null, publicHoliday, now
        )).status();
    }

    private ShopBusinessHour hour(DayType dayType, LocalTime open, LocalTime close, Boolean isClosed, Boolean is24Hours) {
        return ShopBusinessHour.reconstitute(1L, ShopId.of(1L), dayType, open, close, isClosed, is24Hours);
    }

    @Nested
    @DisplayName("영업시간 행 판정")
    class BusinessHourJudgement {
        @Test
        @DisplayName("휴무(isClosed=true) 행이면 시각과 무관하게 준비중")
        void closedRow() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), true, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("24시간(is24Hours=true)은 휴무 플래그보다 우선해 영업중")
        void twentyFourHoursWinsOverClosed() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, null, null, true, true)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(4, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("Boolean 래퍼가 null이면 false와 동일하게 취급")
        void nullBooleanTreatedAsFalse() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), null, null)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(23, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("개점 시각은 포함, 폐점 시각은 제외([start, end))")
        void rangeIsHalfOpen() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(9, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(21, 59)))
                .isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(22, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(8, 59)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("자정 넘김(20:00~02:00) 구간 경계 — 당일 20:00 영업중, 01:59 영업중, 02:00 준비중")
        void crossMidnightBoundaries() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(20, 0), LocalTime.of(2, 0), false, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(20, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(1, 59)))
                .isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(2, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("영업시간 행 선택은 구체성 우선 — 개별 요일이 매일 행을 이긴다")
        void specificDayWinsOverDaily() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false),
                hour(DayType.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("평일 행이 매일 행을 이긴다(주중)")
        void weekdayGroupWinsOverDaily() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false),
                hour(DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("공휴일 행은 공휴일일 때만 선택되고, 평일/주말 그룹 행이 있으면 그쪽이 우선한다")
        void holidayRowSelection() {
            List<ShopBusinessHour> holidayAndDaily = List.of(
                hour(DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false),
                hour(DayType.HOLIDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
            );

            assertThat(statusAt(holidayAndDaily, List.of(), List.of(), true, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);

            assertThat(statusAt(holidayAndDaily, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);

            List<ShopBusinessHour> holidayAndWeekday = List.of(
                hour(DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false),
                hour(DayType.HOLIDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
            );

            assertThat(statusAt(holidayAndWeekday, List.of(), List.of(), true, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("전일 자정 넘김 연장은 공휴일 여부를 false로 두고 전일 행을 고른다")
        void yesterdayExtensionIgnoresHolidayFlag() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(20, 0), LocalTime.of(2, 0), false, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), true, MONDAY.atTime(1, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("개점/폐점 시각이 null이고 24시간·휴무도 아니면 준비중")
        void nullTimesAreNotOpen() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, null, null, false, false)
            );

            assertThat(statusAt(hours, List.of(), List.of(), false, MONDAY.atTime(12, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }
    }

    @Nested
    @DisplayName("휴게시간 판정")
    class BreakTimeJudgement {
        private List<ShopBusinessHour> allDayHours() {
            return List.of(hour(DayType.DAILY, LocalTime.of(0, 0), LocalTime.of(23, 55), false, false));
        }

        @Test
        @DisplayName("시작 시각 포함, 종료 시각 제외")
        void breakRangeIsHalfOpen() {
            List<ShopBreakTime> breaks = List.of(
                ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(15, 0), LocalTime.of(17, 0))
            );

            assertThat(statusAt(allDayHours(), breaks, List.of(), false, MONDAY.atTime(15, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(statusAt(allDayHours(), breaks, List.of(), false, MONDAY.atTime(16, 59)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(statusAt(allDayHours(), breaks, List.of(), false, MONDAY.atTime(17, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("시작·종료 시각이 null이면 휴게시간으로 보지 않는다")
        void nullTimesAreNotBreak() {
            List<ShopBreakTime> breaks = List.of(
                ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.DAILY, null, null)
            );

            assertThat(statusAt(allDayHours(), breaks, List.of(), false, MONDAY.atTime(15, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("요일 구분(dayType)이 오늘과 맞지 않으면 휴게시간이 적용되지 않는다")
        void dayTypeMustMatch() {
            List<ShopBreakTime> weekendBreak = List.of(
                ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.WEEKEND, LocalTime.of(15, 0), LocalTime.of(17, 0))
            );

            assertThat(statusAt(allDayHours(), weekendBreak, List.of(), false, MONDAY.atTime(15, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);

            List<ShopBreakTime> mondayBreak = List.of(
                ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.MONDAY, LocalTime.of(15, 0), LocalTime.of(17, 0))
            );

            assertThat(statusAt(allDayHours(), mondayBreak, List.of(), false, MONDAY.atTime(15, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("공휴일 휴게시간은 공휴일일 때만 적용된다")
        void holidayBreakAppliesOnlyOnHoliday() {
            List<ShopBreakTime> holidayBreak = List.of(
                ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.HOLIDAY, LocalTime.of(15, 0), LocalTime.of(17, 0))
            );

            assertThat(statusAt(allDayHours(), holidayBreak, List.of(), false, MONDAY.atTime(15, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(statusAt(allDayHours(), holidayBreak, List.of(), true, MONDAY.atTime(15, 0)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("자정을 넘기는 휴게시간(23:00~01:00)은 양쪽 끝 구간 모두 적용")
        void breakCrossesMidnight() {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, null, null, false, true)
            );
            List<ShopBreakTime> breaks = List.of(
                ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(23, 0), LocalTime.of(1, 0))
            );

            assertThat(statusAt(hours, breaks, List.of(), false, MONDAY.atTime(23, 30)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(statusAt(hours, breaks, List.of(), false, MONDAY.atTime(0, 30)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(statusAt(hours, breaks, List.of(), false, MONDAY.atTime(1, 0)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("정기휴무(ClosedDayType) 판정")
    class ClosedDayJudgement {
        private ShopOperatingStatus at(ClosedDayType type, LocalDate date) {
            List<ShopBusinessHour> hours = List.of(
                hour(DayType.DAILY, LocalTime.of(0, 0), LocalTime.of(23, 55), false, false)
            );
            return statusAt(hours, List.of(),
                List.of(ShopClosedDay.reconstitute(1L, ShopId.of(1L), type)), false, date.atTime(12, 0));
        }

        @Test
        @DisplayName("연중무휴는 어떤 날짜에도 휴무가 아니다")
        void noClosedDays() {
            assertThat(at(ClosedDayType.NO_CLOSED_DAYS, MONDAY)).isEqualTo(ShopOperatingStatus.OPEN);
            assertThat(at(ClosedDayType.NO_CLOSED_DAYS, LocalDate.of(2026, 7, 6)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("매주 X요일은 해당 요일 전부 휴무, 다른 요일은 영업")
        void everyWeek() {
            assertThat(at(ClosedDayType.EVERY_WEEK_MONDAY, LocalDate.of(2026, 7, 6)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_WEEK_MONDAY, LocalDate.of(2026, 7, 13)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_WEEK_MONDAY, LocalDate.of(2026, 7, 20)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_WEEK_MONDAY, MONDAY))
                .isEqualTo(ShopOperatingStatus.PREPARING);

            assertThat(at(ClosedDayType.EVERY_WEEK_MONDAY, LocalDate.of(2026, 7, 7)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("매주 일요일 — 요일 매칭이 일요일에만 걸린다")
        void everyWeekSunday() {
            assertThat(at(ClosedDayType.EVERY_WEEK_SUNDAY, LocalDate.of(2026, 7, 5)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_WEEK_SUNDAY, LocalDate.of(2026, 7, 4)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("매달 N째 주는 (일자-1)/7+1 기준으로 그 주차에만 휴무")
        void nthWeekOfMonth() {
            assertThat(at(ClosedDayType.EVERY_MONTH_FIRST_WEEK_MONDAY, LocalDate.of(2026, 7, 6)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_MONTH_FIRST_WEEK_MONDAY, LocalDate.of(2026, 7, 13)))
                .isEqualTo(ShopOperatingStatus.OPEN);

            assertThat(at(ClosedDayType.EVERY_MONTH_SECOND_WEEK_MONDAY, LocalDate.of(2026, 7, 13)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_MONTH_SECOND_WEEK_MONDAY, LocalDate.of(2026, 7, 6)))
                .isEqualTo(ShopOperatingStatus.OPEN);

            assertThat(at(ClosedDayType.EVERY_MONTH_THIRD_WEEK_MONDAY, LocalDate.of(2026, 7, 20)))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_MONTH_THIRD_WEEK_MONDAY, LocalDate.of(2026, 7, 13)))
                .isEqualTo(ShopOperatingStatus.OPEN);

            assertThat(at(ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY, MONDAY))
                .isEqualTo(ShopOperatingStatus.PREPARING);
            assertThat(at(ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY, LocalDate.of(2026, 7, 20)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("매달 마지막 주는 '다음 주가 다음 달'인 날에만 휴무")
        void lastWeekOfMonth() {
            assertThat(at(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY, MONDAY))
                .isEqualTo(ShopOperatingStatus.PREPARING);

            assertThat(at(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY, LocalDate.of(2026, 7, 20)))
                .isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("전 상수 × 그 상수가 지정한 요일 — 요일이 다르면 절대 휴무가 아니다")
        void allConstantsNeverMatchOtherWeekdays() {
            for (ClosedDayType type : ClosedDayType.values()) {
                if (type == ClosedDayType.NO_CLOSED_DAYS) {
                    continue;
                }

                for (int day = 1; day <= 31; day++) {
                    LocalDate date = LocalDate.of(2026, 7, day);
                    if (at(type, date) == ShopOperatingStatus.PREPARING) {
                        assertThat(type.name())
                            .as("%s가 %s(%s)에 휴무로 판정됨", type, date, date.getDayOfWeek())
                            .endsWith(date.getDayOfWeek().name());
                    }
                }
            }
        }

        @Test
        @DisplayName("매주 X요일 상수는 7월 안에서 정확히 그 요일 수만큼 휴무가 된다")
        void everyWeekConstantsMatchAllOccurrences() {
            for (ClosedDayType type : ClosedDayType.values()) {
                if (!type.name().startsWith("EVERY_WEEK_")) {
                    continue;
                }
                long closedCount = 0;
                long weekdayCount = 0;
                for (int day = 1; day <= 31; day++) {
                    LocalDate date = LocalDate.of(2026, 7, day);
                    if (type.name().endsWith(date.getDayOfWeek().name())) {
                        weekdayCount++;
                    }
                    if (at(type, date) == ShopOperatingStatus.PREPARING) {
                        closedCount++;
                    }
                }
                assertThat(closedCount).as("%s", type).isEqualTo(weekdayCount);
            }
        }

        @Test
        @DisplayName("매달 N째 주 상수는 7월 안에서 정확히 1일만 휴무가 된다")
        void monthlyConstantsMatchExactlyOneDay() {
            for (ClosedDayType type : ClosedDayType.values()) {
                if (!type.name().startsWith("EVERY_MONTH_")) {
                    continue;
                }
                long closedCount = 0;
                for (int day = 1; day <= 31; day++) {
                    if (at(type, LocalDate.of(2026, 7, day)) == ShopOperatingStatus.PREPARING) {
                        closedCount++;
                    }
                }
                assertThat(closedCount).as("%s", type).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("가게 상태·공휴일 우선순위")
    class ShopLevelJudgement {
        private List<ShopBusinessHour> allDayHours() {
            return List.of(hour(DayType.DAILY, null, null, false, true));
        }

        @Test
        @DisplayName("폐업 가게는 영업시간과 무관하게 준비중")
        void permanentlyClosedShop() {
            ShopOperatingStatus status = statusAt(
                shop(true, false), allDayHours(), List.of(), List.of(), false, MONDAY.atTime(12, 0)
            );

            assertThat(status).isEqualTo(ShopOperatingStatus.PREPARING);
        }

        @Test
        @DisplayName("공휴일 휴무 설정 가게는 공휴일에만 준비중")
        void closedOnPublicHolidays() {
            assertThat(statusAt(
                shop(false, true), allDayHours(), List.of(), List.of(), true, MONDAY.atTime(12, 0)
            )).isEqualTo(ShopOperatingStatus.PREPARING);

            assertThat(statusAt(
                shop(false, true), allDayHours(), List.of(), List.of(), false, MONDAY.atTime(12, 0)
            )).isEqualTo(ShopOperatingStatus.OPEN);
        }

        @Test
        @DisplayName("공휴일이어도 가게가 공휴일 휴무 설정이 아니면 영업중")
        void publicHolidayWithoutSetting() {
            assertThat(statusAt(
                shop(false, false), allDayHours(), List.of(), List.of(), true, MONDAY.atTime(12, 0)
            )).isEqualTo(ShopOperatingStatus.OPEN);
        }
    }
}
