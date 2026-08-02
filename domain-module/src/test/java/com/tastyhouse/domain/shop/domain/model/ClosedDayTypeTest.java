package com.tastyhouse.domain.shop.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 정기휴무 주기 판정 단위 테스트.
 *
 * <p>판정이 상수 <b>이름 문자열 파싱</b>에서 <b>필드 기반</b>({@code dayOfWeek}/{@code weekOrdinal})으로
 * 이식됐으므로, 전 상수가 자기 이름이 뜻하는 요일·주차와 실제로 일치하는지 전수 검증한다.
 */
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
        // catch-all로 삼키지 않는다 — 이름 규칙에서 벗어난 상수가 추가되면 조용히 통과시키지 말고 실패시킨다
        // (이 리팩터링이 없애려 한 "문자열 fallthrough" 결함을 테스트가 되풀이하지 않도록).
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
        // 2026년 7월 월요일: 6(1주) 13(2주) 20(3주) 27(4주)
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
        // 2026-07-27(월) + 1주 = 8/3 → 마지막 월요일
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 27))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 7, 20))).isFalse();

        // 2026-08-31(월) + 1주 = 9/7 → 마지막 월요일
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 31))).isTrue();
    }

    @Test
    @DisplayName("5주차가 존재하는 달에서 넷째 주와 마지막 주는 서로 다른 날일 수 있다")
    void fourthAndLastCanDiffer() {
        // 2026-08 월요일: 3(1주) 10(2주) 17(3주) 24(4주) 31(5주=마지막)
        assertThat(ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 24))).isTrue();
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 24))).isFalse();
        assertThat(ClosedDayType.EVERY_MONTH_LAST_WEEK_MONDAY.matches(LocalDate.of(2026, 8, 31))).isTrue();
    }

    @Test
    @DisplayName("일요일 상수는 일요일에만 매칭된다(요일 경계 확인)")
    void sundayConstant() {
        // 2026-07-05는 일요일
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
