package com.tastyhouse.domain.holiday.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 법정 공휴일 판정 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 조회 포트를 손으로 만든 fake로 대체해 검증한다(domain-module에는 Mockito 의존이 없다).
 */
class PublicHolidayCalendarTest {

    /** 2026-08-02는 평범한 일요일이다(법정공휴일 아님). */
    private static final LocalDate PLAIN_SUNDAY = LocalDate.of(2026, 8, 2);

    /** 2026-03-01 삼일절은 일요일과 겹치는 법정공휴일이다. */
    private static final LocalDate HOLIDAY_ON_SUNDAY = LocalDate.of(2026, 3, 1);

    @Nested
    @DisplayName("isPublicHoliday - 일요일 규칙")
    class SundayRule {

        @Test
        @DisplayName("캘린더가 일요일 자체를 담지 않는다는 데이터 규칙 덕에, 평범한 일요일은 공휴일이 아니다(공휴일 배달팁 미부과)")
        void isPublicHoliday_falseForPlainSunday() {
            PublicHolidayCalendar calendar = calendarWith(HOLIDAY_ON_SUNDAY);

            assertThat(PLAIN_SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(calendar.isPublicHoliday(PLAIN_SUNDAY)).isFalse();
        }

        @Test
        @DisplayName("캘린더가 일요일 자체를 담지 않는다는 데이터 규칙 덕에, 법정공휴일과 겹친 일요일은 캘린더에 있어 공휴일이다(공휴일 배달팁 부과)")
        void isPublicHoliday_trueForHolidayFallingOnSunday() {
            PublicHolidayCalendar calendar = calendarWith(HOLIDAY_ON_SUNDAY);

            assertThat(HOLIDAY_ON_SUNDAY.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(calendar.isPublicHoliday(HOLIDAY_ON_SUNDAY)).isTrue();
        }
    }

    @Nested
    @DisplayName("isPublicHoliday - 기타")
    class IsPublicHoliday {

        @Test
        @DisplayName("캘린더에 있는 평일 공휴일은 true다")
        void isPublicHoliday_trueForWeekdayHoliday() {
            LocalDate childrensDay = LocalDate.of(2026, 5, 5);
            PublicHolidayCalendar calendar = calendarWith(childrensDay);

            assertThat(calendar.isPublicHoliday(childrensDay)).isTrue();
        }

        @Test
        @DisplayName("null 날짜는 false다(판정 불가를 예외가 아니라 미부과로 처리)")
        void isPublicHoliday_falseForNull() {
            PublicHolidayCalendar calendar = calendarWith(HOLIDAY_ON_SUNDAY);

            assertThat(calendar.isPublicHoliday(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("findBetween")
    class FindBetween {

        @Test
        @DisplayName("구간 안의 공휴일 날짜 집합을 돌려준다(양끝 포함)")
        void findBetween_returnsHolidaysInRange() {
            LocalDate first = LocalDate.of(2026, 5, 5);
            LocalDate second = LocalDate.of(2026, 5, 24);
            PublicHolidayCalendar calendar = calendarWith(first, second);

            assertThat(calendar.findBetween(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)))
                .containsExactlyInAnyOrder(first, second);
        }

        @Test
        @DisplayName("from이 to보다 늦은 역순 구간이면 빈 집합이다")
        void findBetween_emptyForReversedRange() {
            PublicHolidayCalendar calendar = calendarWith(LocalDate.of(2026, 5, 5));

            assertThat(calendar.findBetween(LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1))).isEmpty();
        }

        @Test
        @DisplayName("null 경계는 빈 집합이다")
        void findBetween_emptyForNullBounds() {
            PublicHolidayCalendar calendar = calendarWith(LocalDate.of(2026, 5, 5));

            assertThat(calendar.findBetween(null, LocalDate.of(2026, 5, 31))).isEmpty();
            assertThat(calendar.findBetween(LocalDate.of(2026, 5, 1), null)).isEmpty();
        }
    }

    private static PublicHolidayCalendar calendarWith(LocalDate... holidayDates) {
        PublicHolidayRepositoryFake repository = new PublicHolidayRepositoryFake();
        for (LocalDate holidayDate : holidayDates) {
            repository.add(holidayDate);
        }
        return new PublicHolidayCalendar(repository);
    }

    private static final class PublicHolidayRepositoryFake implements PublicHolidayRepository {

        private final Map<LocalDate, PublicHoliday> holidays = new LinkedHashMap<>();
        private long sequence = 0L;

        void add(LocalDate holidayDate) {
            holidays.put(holidayDate, PublicHoliday.reconstitute(++sequence, holidayDate, "공휴일", false));
        }

        @Override
        public boolean existsByHolidayDate(LocalDate holidayDate) {
            return holidays.containsKey(holidayDate);
        }

        @Override
        public List<PublicHoliday> findAllByHolidayDateBetween(LocalDate from, LocalDate to) {
            List<PublicHoliday> found = new ArrayList<>();
            for (Map.Entry<LocalDate, PublicHoliday> entry : holidays.entrySet()) {
                LocalDate holidayDate = entry.getKey();
                if (!holidayDate.isBefore(from) && !holidayDate.isAfter(to)) {
                    found.add(entry.getValue());
                }
            }
            return found;
        }
    }
}
