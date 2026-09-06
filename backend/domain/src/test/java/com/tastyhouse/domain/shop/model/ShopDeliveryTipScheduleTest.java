package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 시간별 추가 배달팁 한 행의 값 불변식·시간 포함 판정 단위 테스트.
 *
 * <p>같은 요일 구분끼리의 시간대 겹침(집합 관계)은 {@code ShopDeliveryTipService}가 담당한다.
 */
class ShopDeliveryTipScheduleTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);

    @Nested
    @DisplayName("covers - 자정 넘김 구간")
    class CoversOvernight {

        @Test
        @DisplayName("22:00~02:00 구간은 23:00과 01:00을 포함하고 03:00·21:00은 포함하지 않는다")
        void covers_overnightRange() {
            ShopDeliveryTipSchedule schedule = schedule(DayType.DAILY, LocalTime.of(22, 0), LocalTime.of(2, 0), 1000);

            assertThat(schedule.covers(LocalTime.of(23, 0), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(schedule.covers(LocalTime.of(1, 0), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(schedule.covers(LocalTime.of(3, 0), DayOfWeek.MONDAY, false)).isFalse();
            assertThat(schedule.covers(LocalTime.of(21, 0), DayOfWeek.MONDAY, false)).isFalse();
        }
    }

    @Nested
    @DisplayName("covers - 일반 구간")
    class CoversNormal {

        @Test
        @DisplayName("반열림 구간 [시작, 종료)이라 시작 시각은 포함하고 종료 시각은 포함하지 않는다")
        void covers_halfOpenRange() {
            ShopDeliveryTipSchedule schedule = schedule(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000);

            assertThat(schedule.covers(LocalTime.of(18, 0), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(schedule.covers(LocalTime.of(20, 59), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(schedule.covers(LocalTime.of(21, 0), DayOfWeek.MONDAY, false)).isFalse();
            assertThat(schedule.covers(LocalTime.of(17, 59), DayOfWeek.MONDAY, false)).isFalse();
        }
    }

    @Nested
    @DisplayName("covers - dayType 위임")
    class CoversDayType {

        @Test
        @DisplayName("MONDAY 행은 월요일에만 적용되고 화요일에는 적용되지 않는다(DayType.appliesTo에 위임)")
        void covers_delegatesToDayType() {
            ShopDeliveryTipSchedule schedule = schedule(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000);

            assertThat(schedule.covers(LocalTime.of(19, 0), DayOfWeek.MONDAY, false)).isTrue();
            assertThat(schedule.covers(LocalTime.of(19, 0), DayOfWeek.TUESDAY, false)).isFalse();
        }

        @Test
        @DisplayName("WEEKDAY 행은 평일에만, WEEKEND 행은 주말에만 적용된다")
        void covers_weekGroupDayTypes() {
            ShopDeliveryTipSchedule weekday = schedule(DayType.WEEKDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000);
            ShopDeliveryTipSchedule weekend = schedule(DayType.WEEKEND, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000);

            assertThat(weekday.covers(LocalTime.of(19, 0), DayOfWeek.WEDNESDAY, false)).isTrue();
            assertThat(weekday.covers(LocalTime.of(19, 0), DayOfWeek.SATURDAY, false)).isFalse();
            assertThat(weekend.covers(LocalTime.of(19, 0), DayOfWeek.SATURDAY, false)).isTrue();
            assertThat(weekend.covers(LocalTime.of(19, 0), DayOfWeek.WEDNESDAY, false)).isFalse();
        }
    }

    @Nested
    @DisplayName("of - 검증")
    class Validation {

        @Test
        @DisplayName("DayType.HOLIDAY는 공휴일 전용 애그리거트와 이중 부과되므로 거부한다")
        void of_rejectsHolidayDayType() {
            assertThatThrownBy(() -> ShopDeliveryTipSchedule.of(
                SHOP_ID, DayType.HOLIDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_DAY_TYPE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("dayType이 null이면 SHOP_DELIVERY_TIP_SCHEDULE_DAY_TYPE_NOT_ALLOWED로 거부한다")
        void of_rejectsNullDayType() {
            assertThatThrownBy(() -> ShopDeliveryTipSchedule.of(
                SHOP_ID, null, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_DAY_TYPE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("시작과 종료가 같으면 길이 0인지 24시간인지 갈리므로 거부한다")
        void of_rejectsSameStartAndEnd() {
            assertThatThrownBy(() -> ShopDeliveryTipSchedule.of(
                SHOP_ID, DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(18, 0), 1000
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP);
        }

        @ParameterizedTest(name = "금액 {0}원은 통과한다")
        @ValueSource(ints = {0, 5000, 10000})
        @DisplayName("추가 배달팁 금액 0~10,000원은 허용한다")
        void of_allowsAmountWithinRange(int tipAmount) {
            ShopDeliveryTipSchedule schedule = schedule(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), tipAmount);

            assertThat(schedule.getTipAmount()).isEqualTo(tipAmount);
        }

        @ParameterizedTest(name = "금액 {0}원은 거부한다")
        @ValueSource(ints = {-1, 10001})
        @DisplayName("0~10,000원을 벗어나면 SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE로 거부한다")
        void of_rejectsAmountOutOfRange(int tipAmount) {
            assertThatThrownBy(() -> ShopDeliveryTipSchedule.of(
                SHOP_ID, DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), tipAmount
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("생성과 같은 검증 한 벌을 강제한다 — HOLIDAY로의 변경도 막는다")
        void update_appliesSameValidation() {
            ShopDeliveryTipSchedule schedule = schedule(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000);

            assertThatThrownBy(() -> schedule.update(DayType.HOLIDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_DAY_TYPE_NOT_ALLOWED);

            schedule.update(DayType.SATURDAY, LocalTime.of(11, 0), LocalTime.of(14, 0), 2000);

            assertThat(schedule.getDayType()).isEqualTo(DayType.SATURDAY);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(schedule.getTipAmount()).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("검증하지 않는다 — HOLIDAY 행이나 범위 밖 금액도 로드할 수 있다")
        void reconstitute_bypassesValidation() {
            ShopDeliveryTipSchedule schedule = ShopDeliveryTipSchedule.reconstitute(
                7L, SHOP_ID, DayType.HOLIDAY, LocalTime.of(18, 0), LocalTime.of(18, 0), 99999
            );

            assertThat(schedule.getId()).isEqualTo(7L);
            assertThat(schedule.getDayType()).isEqualTo(DayType.HOLIDAY);
            assertThat(schedule.getTipAmount()).isEqualTo(99999);
        }
    }

    private static ShopDeliveryTipSchedule schedule(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        return ShopDeliveryTipSchedule.of(SHOP_ID, dayType, startTime, endTime, tipAmount);
    }
}
