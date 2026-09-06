package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClosedDayTypeTest {
    @Test
    @DisplayName("연중무휴는 어떤 날짜에도 휴무가 아니다")
    void noClosedDaysNeverMatches() {
        for (int day = 1; day <= 31; day++) {
            assertThat(ClosedDayType.NO_CLOSED_DAYS.matches(LocalDate.of(2026, 7, day))).isFalse();
        }
        assertThat(ClosedDayType.NO_CLOSED_DAYS.getDayOfWeek()).isNull();
        assertThat(ClosedDayType.NO_CLOSED_DAYS.getWeekOrdinal()).isNull();
    }

    @Test
    @DisplayName("전 상수의 dayOfWeek 필드는 상수 이름이 끝나는 요일과 일치한다")
    void dayOfWeekFieldMatchesConstantName() {
        for (ClosedDayType type : ClosedDayType.values()) {
            if (type == ClosedDayType.NO_CLOSED_DAYS) {
                continue;
            }
            assertThat(type.getDayOfWeek())
                .as("%s", type)
                .isNotNull();
            assertThat(type.name())
                .as("%s의 dayOfWeek=%s", type, type.getDayOfWeek())
                .endsWith(type.getDayOfWeek().name());
        }
    }

    @Test
    @DisplayName("전 상수의 weekOrdinal 필드는 상수 이름이 나타내는 주기와 일치한다")
    void weekOrdinalFieldMatchesConstantName() {
        for (ClosedDayType type : ClosedDayType.values()) {
            if (type == ClosedDayType.NO_CLOSED_DAYS) {
                continue;
            }
            ClosedDayType.WeekOrdinal expected = expectedOrdinal(type.name());
            assertThat(type.getWeekOrdinal()).as("%s", type).isEqualTo(expected);
        }
    }

    private ClosedDayType.WeekOrdinal expectedOrdinal(String name) {
        if (name.startsWith("EVERY_WEEK_")) {
            return ClosedDayType.WeekOrdinal.EVERY;
        }
        if (name.contains("FIRST_WEEK")) {
            return ClosedDayType.WeekOrdinal.FIRST;
        }
        if (name.contains("SECOND_WEEK")) {
            return ClosedDayType.WeekOrdinal.SECOND;
        }
        if (name.contains("THIRD_WEEK")) {
            return ClosedDayType.WeekOrdinal.THIRD;
        }
        if (name.contains("FOURTH_WEEK")) {
            return ClosedDayType.WeekOrdinal.FOURTH;
        }
        if (name.contains("LAST_WEEK")) {
            return ClosedDayType.WeekOrdinal.LAST;
        }

        throw new IllegalArgumentException("알 수 없는 정기휴무 주기 이름: " + name);
    }

    @Test
    @DisplayName("요일이 다르면 어떤 주기든 매칭되지 않는다")
    void neverMatchesOtherWeekdays() {
        for (ClosedDayType type : ClosedDayType.values()) {
            if (type == ClosedDayType.NO_CLOSED_DAYS) {
                continue;
            }
            for (int day = 1; day <= 31; day++) {
                LocalDate date = LocalDate.of(2026, 7, day);
                if (date.getDayOfWeek() != type.getDayOfWeek()) {
                    assertThat(type.matches(date))
                        .as("%s가 %s(%s)에 매칭됨", type, date, date.getDayOfWeek())
                        .isFalse();
                }
            }
        }
    }

    @Test
    @DisplayName("매주 X요일은 그 달의 해당 요일 전부에 매칭된다")
    void everyWeekMatchesAllOccurrences() {
        for (ClosedDayType type : ClosedDayType.values()) {
            if (type.getWeekOrdinal() != ClosedDayType.WeekOrdinal.EVERY) {
                continue;
            }
            for (int day = 1; day <= 31; day++) {
                LocalDate date = LocalDate.of(2026, 7, day);
                boolean sameWeekday = date.getDayOfWeek() == type.getDayOfWeek();
                assertThat(type.matches(date)).as("%s @ %s", type, date).isEqualTo(sameWeekday);
            }
        }
    }

    @Test
    @DisplayName("매달 N째 주 상수는 한 달에 정확히 한 번만 매칭된다")
    void monthlyOrdinalMatchesExactlyOnce() {
        for (ClosedDayType type : ClosedDayType.values()) {
            if (type == ClosedDayType.NO_CLOSED_DAYS
                || type.getWeekOrdinal() == ClosedDayType.WeekOrdinal.EVERY) {
                continue;
            }
            for (int month = 1; month <= 12; month++) {
                LocalDate first = LocalDate.of(2026, month, 1);
                long matched = 0;
                for (int day = 1; day <= first.lengthOfMonth(); day++) {
                    if (type.matches(LocalDate.of(2026, month, day))) {
                        matched++;
                    }
                }
                assertThat(matched).as("%s @ 2026-%02d", type, month).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("첫째~넷째 주는 (일자-1)/7+1 기준 주차에 매칭된다")
    void nthWeekUsesDayOfMonthDivision() {
        assertThat(ClosedDayType.EVERY_MONTH_FIRST_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 6))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_SECOND_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 13))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_THIRD_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 20))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 27))).isTrue();

        assertThat(ClosedDayType.EVERY_MONTH_FIRST_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 13))).isFalse();
        assertThat(ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 20))).isFalse();
    }

    @Test
    @DisplayName("마지막 주는 '1주 뒤가 다음 달'인 날에 매칭된다")
    void lastWeekUsesNextWeekMonth() {
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 27))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 20))).isFalse();

        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 31))).isTrue();
    }

    @Test
    @DisplayName("5주차가 존재하는 달에서 넷째 주와 마지막 주는 서로 다른 날일 수 있다")
    void fourthAndLastCanDiffer() {
        assertThat(ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 24))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 24))).isFalse();
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 31))).isTrue();
    }

    @Test
    @DisplayName("일요일 상수는 일요일에만 매칭된다(요일 경계 확인)")
    void sundayConstant() {
        assertThat(ClosedDayType.EVERY_WEEK_SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(ClosedDayType.EVERY_WEEK_SUNDAY.matches(LocalDate.of(2026, 7, 5))).isTrue();
        assertThat(ClosedDayType.EVERY_WEEK_SUNDAY.matches(LocalDate.of(2026, 7, 4))).isFalse();
        assertThat(ClosedDayType.EVERY_WEEK_SUNDAY.matches(LocalDate.of(2026, 7, 6))).isFalse();
    }

    @Test
    @DisplayName("from은 알 수 없는 코드에 BusinessException을 던진다")
    void fromRejectsUnknownCode() {
        assertThat(ClosedDayType.from("EVERY_WEEK_MONDAY")).isEqualTo(ClosedDayType.EVERY_WEEK_MONDAY);
        assertThatThrownBy(() -> ClosedDayType.from("NOT_A_TYPE"))
            .isInstanceOf(BusinessException.class);
    }
}
