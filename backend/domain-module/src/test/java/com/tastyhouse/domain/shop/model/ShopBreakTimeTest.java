package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.vo.ShopId;

class ShopBreakTimeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBreakTime() {
        ShopBreakTime breakTime = ShopBreakTime.of(ShopId.of(1L), DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

        assertThat(breakTime.getId()).isNull();
        assertThat(breakTime.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(breakTime.getDayType()).isEqualTo(DayType.WEEKDAY);
        assertThat(breakTime.getStartTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(breakTime.getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("update는 브레이크타임 정보를 변경한다")
    void update_changesFields() {
        ShopBreakTime breakTime = ShopBreakTime.of(ShopId.of(1L), DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

        breakTime.update(DayType.SATURDAY, LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertThat(breakTime.getDayType()).isEqualTo(DayType.SATURDAY);
        assertThat(breakTime.getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(breakTime.getEndTime()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBreakTime breakTime = ShopBreakTime.reconstitute(1L, ShopId.of(2L), DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

        assertThat(breakTime.getId()).isEqualTo(1L);
        assertThat(breakTime.getShopId()).isEqualTo(ShopId.of(2L));
    }

    @Nested
    @DisplayName("covers — 휴게시간 포함 판정")
    class Covers {

        private ShopBreakTime breakTime(DayType dayType, LocalTime start, LocalTime end) {
            return ShopBreakTime.reconstitute(1L, ShopId.of(1L), dayType, start, end);
        }

        @Test
        @DisplayName("시작 시각은 포함하고 종료 시각은 제외한다")
        void halfOpenRange() {
            ShopBreakTime target = breakTime(DayType.DAILY, LocalTime.of(15, 0), LocalTime.of(17, 0));

            assertThat(target.covers(LocalTime.of(14, 59), DayOfWeek.MONDAY, false)).isFalse();
            assertThat(target.covers(LocalTime.of(15, 0), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(target.covers(LocalTime.of(16, 59), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(target.covers(LocalTime.of(17, 0), DayOfWeek.MONDAY, false)).isFalse();
        }

        @Test
        @DisplayName("자정을 넘기는 휴게시간은 양쪽 조각 모두 포함한다")
        void crossesMidnight() {
            ShopBreakTime target = breakTime(DayType.DAILY, LocalTime.of(23, 0), LocalTime.of(1, 0));

            assertThat(target.covers(LocalTime.of(23, 30), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(target.covers(LocalTime.of(0, 30), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(target.covers(LocalTime.of(1, 0), DayOfWeek.MONDAY, false)).isFalse();
            assertThat(target.covers(LocalTime.of(12, 0), DayOfWeek.MONDAY, false)).isFalse();
        }

        @Test
        @DisplayName("시작·종료 시각이 없으면 포함하지 않는다")
        void nullTimes() {
            assertThat(breakTime(DayType.DAILY, null, null).covers(LocalTime.of(15, 0), DayOfWeek.MONDAY, false)).isFalse();
            assertThat(breakTime(DayType.DAILY, LocalTime.of(15, 0), null).covers(LocalTime.of(15, 0), DayOfWeek.MONDAY, false)).isFalse();
            assertThat(breakTime(DayType.DAILY, null, LocalTime.of(17, 0)).covers(LocalTime.of(15, 0), DayOfWeek.MONDAY, false)).isFalse();
        }

        @Test
        @DisplayName("개별 요일 휴게시간은 그 요일에만 적용된다")
        void specificDay() {
            ShopBreakTime target = breakTime(DayType.MONDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

            assertThat(target.covers(LocalTime.of(15, 30), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(target.covers(LocalTime.of(15, 30), DayOfWeek.TUESDAY, false)).isFalse();
        }

        @Test
        @DisplayName("평일 휴게시간은 주중에만, 주말 휴게시간은 주말에만 적용된다")
        void weekdayAndWeekend() {
            ShopBreakTime weekday = breakTime(DayType.WEEKDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));
            ShopBreakTime weekend = breakTime(DayType.WEEKEND, LocalTime.of(15, 0), LocalTime.of(17, 0));

            assertThat(weekday.covers(LocalTime.of(15, 30), DayOfWeek.FRIDAY, false)).isTrue();
            assertThat(weekday.covers(LocalTime.of(15, 30), DayOfWeek.SATURDAY, false)).isFalse();
            assertThat(weekend.covers(LocalTime.of(15, 30), DayOfWeek.SATURDAY, false)).isTrue();
            assertThat(weekend.covers(LocalTime.of(15, 30), DayOfWeek.SUNDAY, false)).isTrue();
            assertThat(weekend.covers(LocalTime.of(15, 30), DayOfWeek.FRIDAY, false)).isFalse();
        }

        @Test
        @DisplayName("공휴일 휴게시간은 공휴일일 때만 적용된다")
        void holiday() {
            ShopBreakTime target = breakTime(DayType.HOLIDAY, LocalTime.of(15, 0), LocalTime.of(17, 0));

            assertThat(target.covers(LocalTime.of(15, 30), DayOfWeek.MONDAY, true)).isTrue();
            assertThat(target.covers(LocalTime.of(15, 30), DayOfWeek.MONDAY, false)).isFalse();
        }

        @Test
        @DisplayName("매일 휴게시간은 요일·공휴일과 무관하게 적용된다")
        void daily() {
            ShopBreakTime target = breakTime(DayType.DAILY, LocalTime.of(15, 0), LocalTime.of(17, 0));

            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                assertThat(target.covers(LocalTime.of(15, 30), dayOfWeek, false)).isTrue();
                assertThat(target.covers(LocalTime.of(15, 30), dayOfWeek, true)).isTrue();
            }
        }
    }
}
