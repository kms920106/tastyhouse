package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

class ShopBusinessHourTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBusinessHour() {
        ShopBusinessHour businessHour = ShopBusinessHour.of(ShopId.of(1L), DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false);

        assertThat(businessHour.getId()).isNull();
        assertThat(businessHour.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(businessHour.getDayType()).isEqualTo(DayType.WEEKDAY);
        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(businessHour.getIsClosed()).isFalse();
        assertThat(businessHour.getIs24Hours()).isFalse();
    }

    @Test
    @DisplayName("update는 영업시간 정보를 변경한다")
    void update_changesFields() {
        ShopBusinessHour businessHour = ShopBusinessHour.of(ShopId.of(1L), DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false);

        businessHour.update(DayType.SUNDAY, LocalTime.of(10, 0), LocalTime.of(20, 0), true, false);

        assertThat(businessHour.getDayType()).isEqualTo(DayType.SUNDAY);
        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(businessHour.getIsClosed()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBusinessHour businessHour = ShopBusinessHour.reconstitute(
            1L, ShopId.of(2L), DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false
        );

        assertThat(businessHour.getId()).isEqualTo(1L);
        assertThat(businessHour.getShopId()).isEqualTo(ShopId.of(2L));
    }

    private static ShopBusinessHour hourOf(LocalTime openTime, LocalTime closeTime) {
        return ShopBusinessHour.of(ShopId.of(1L), DayType.WEEKDAY, openTime, closeTime, false, false);
    }

    @Test
    @DisplayName("of는 최소 1시간 경계값을 통과시킨다")
    void of_minimumDuration_passes() {
        assertThat(hourOf(LocalTime.of(9, 0), LocalTime.of(10, 0)).getCloseTime())
            .isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("of는 최대 23시간 55분 경계값을 통과시킨다")
    void of_maximumDuration_passes() {
        assertThat(hourOf(LocalTime.of(0, 0), LocalTime.of(23, 55)).getCloseTime())
            .isEqualTo(LocalTime.of(23, 55));
    }

    @Test
    @DisplayName("of는 자정을 넘기는 영업시간을 다음날로 계산해 통과시킨다")
    void of_overnightRange_passes() {
        assertThat(hourOf(LocalTime.of(22, 0), LocalTime.of(2, 0)).getOpenTime())
            .isEqualTo(LocalTime.of(22, 0));
    }

    @Test
    @DisplayName("of는 1시간 미만이면 SHOP_BUSINESS_HOUR_INVALID_RANGE로 거부한다")
    void of_belowMinimumDuration_throws() {
        assertThatThrownBy(() -> hourOf(LocalTime.of(9, 0), LocalTime.of(9, 55)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
    }

    @Test
    @DisplayName("of는 24시간(=경계 초과)이면 SHOP_BUSINESS_HOUR_INVALID_RANGE로 거부한다")
    void of_exceedingMaximumDuration_throws() {
        assertThatThrownBy(() -> hourOf(LocalTime.of(9, 0), LocalTime.of(9, 0)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
    }

    @Test
    @DisplayName("of는 5분 단위가 아니면 SHOP_BUSINESS_HOUR_INVALID_UNIT으로 거부한다")
    void of_notFiveMinuteUnit_throws() {
        assertThatThrownBy(() -> hourOf(LocalTime.of(9, 3), LocalTime.of(22, 0)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);

        assertThatThrownBy(() -> hourOf(LocalTime.of(9, 0), LocalTime.of(22, 7)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);
    }

    @Test
    @DisplayName("of는 휴무·24시간 영업이면 시간 검증을 생략한다(openTime/closeTime null 허용)")
    void of_closedOr24Hours_skipsTimeValidation() {
        assertThat(ShopBusinessHour.of(ShopId.of(1L), DayType.WEEKDAY, null, null, true, false).getIsClosed()).isTrue();
        assertThat(ShopBusinessHour.of(ShopId.of(1L), DayType.WEEKDAY, null, null, false, true).getIs24Hours()).isTrue();
    }

    @Test
    @DisplayName("of는 휴무·24시간이 아닌데 시간이 null이면 SHOP_BUSINESS_HOUR_INVALID_RANGE로 거부한다")
    void of_nullTimesWhenOpen_throws() {
        assertThatThrownBy(() -> hourOf(null, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
    }

    @Test
    @DisplayName("update도 of와 같은 규격 불변식을 강제한다(서비스를 거치지 않아도 강제됨)")
    void update_enforcesSameInvariants() {
        ShopBusinessHour businessHour = hourOf(LocalTime.of(9, 0), LocalTime.of(22, 0));

        assertThatThrownBy(() -> businessHour.update(
            DayType.WEEKDAY, LocalTime.of(9, 3), LocalTime.of(22, 0), false, false
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);

        // 실패한 update는 기존 상태를 바꾸지 않는다
        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    @DisplayName("reconstitute는 규격 검증을 하지 않는다(불변식 위반 레거시 행도 로드 가능)")
    void reconstitute_bypassesSpecValidation() {
        ShopBusinessHour businessHour = ShopBusinessHour.reconstitute(
            1L, ShopId.of(2L), DayType.WEEKDAY, LocalTime.of(9, 3), LocalTime.of(9, 33), false, false
        );

        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(9, 3));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(9, 33));
    }

    @Nested
    @DisplayName("isOpenAt — 영업 중 판정")
    class IsOpenAt {

        private ShopBusinessHour hour(LocalTime open, LocalTime close, Boolean isClosed, Boolean is24Hours) {
            return ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, open, close, isClosed, is24Hours);
        }

        @Test
        @DisplayName("24시간 영업은 어떤 시각에도 영업중")
        void twentyFourHours() {
            ShopBusinessHour businessHour = hour(null, null, false, true);

            assertThat(businessHour.isOpenAt(LocalTime.of(0, 0))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(12, 0))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(23, 59))).isTrue();
        }

        @Test
        @DisplayName("24시간 영업은 휴무 플래그보다 우선한다")
        void twentyFourHoursWinsOverClosed() {
            assertThat(hour(null, null, true, true).isOpenAt(LocalTime.of(12, 0))).isTrue();
        }

        @Test
        @DisplayName("휴무 표시 행은 어떤 시각에도 영업이 아니다")
        void closedRow() {
            ShopBusinessHour businessHour = hour(LocalTime.of(9, 0), LocalTime.of(22, 0), true, false);

            assertThat(businessHour.isOpenAt(LocalTime.of(12, 0))).isFalse();
        }

        @Test
        @DisplayName("개점 시각은 포함하고 폐점 시각은 제외한다")
        void halfOpenRange() {
            ShopBusinessHour businessHour = hour(LocalTime.of(9, 0), LocalTime.of(22, 0), false, false);

            assertThat(businessHour.isOpenAt(LocalTime.of(8, 59))).isFalse();
            assertThat(businessHour.isOpenAt(LocalTime.of(9, 0))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(21, 59))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(22, 0))).isFalse();
        }

        @Test
        @DisplayName("자정을 넘기는 영업시간은 양쪽 조각 모두 영업중")
        void crossesMidnight() {
            ShopBusinessHour businessHour = hour(LocalTime.of(20, 0), LocalTime.of(2, 0), false, false);

            assertThat(businessHour.isOpenAt(LocalTime.of(19, 59))).isFalse();
            assertThat(businessHour.isOpenAt(LocalTime.of(20, 0))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(23, 59))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(0, 0))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(1, 59))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(2, 0))).isFalse();
        }

        @Test
        @DisplayName("Boolean 래퍼가 null이면 false로 정규화한다")
        void nullBooleanNormalizedToFalse() {
            ShopBusinessHour businessHour = hour(LocalTime.of(9, 0), LocalTime.of(22, 0), null, null);

            assertThat(businessHour.isClosed()).isFalse();
            assertThat(businessHour.is24Hours()).isFalse();
            assertThat(businessHour.isOpenAt(LocalTime.of(12, 0))).isTrue();
            assertThat(businessHour.isOpenAt(LocalTime.of(23, 0))).isFalse();
        }

        @Test
        @DisplayName("개점·폐점 시각이 없고 24시간도 아니면 영업이 아니다")
        void nullTimes() {
            assertThat(hour(null, null, false, false).isOpenAt(LocalTime.of(12, 0))).isFalse();
            assertThat(hour(LocalTime.of(9, 0), null, false, false).isOpenAt(LocalTime.of(12, 0))).isFalse();
            assertThat(hour(null, LocalTime.of(22, 0), false, false).isOpenAt(LocalTime.of(12, 0))).isFalse();
        }
    }

    @Nested
    @DisplayName("extendsIntoNextDayAt — 전일 자정 넘김 연장 판정")
    class ExtendsIntoNextDayAt {

        private ShopBusinessHour hour(LocalTime open, LocalTime close, Boolean isClosed, Boolean is24Hours) {
            return ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, open, close, isClosed, is24Hours);
        }

        @Test
        @DisplayName("자정을 넘기는 행은 폐점 시각 전까지 연장 구간이다")
        void crossesMidnight() {
            ShopBusinessHour businessHour = hour(LocalTime.of(20, 0), LocalTime.of(2, 0), false, false);

            assertThat(businessHour.extendsIntoNextDayAt(LocalTime.of(0, 0))).isTrue();
            assertThat(businessHour.extendsIntoNextDayAt(LocalTime.of(1, 59))).isTrue();
            assertThat(businessHour.extendsIntoNextDayAt(LocalTime.of(2, 0))).isFalse();
            assertThat(businessHour.extendsIntoNextDayAt(LocalTime.of(21, 0))).isFalse();
        }

        @Test
        @DisplayName("자정을 넘기지 않는 행은 연장 구간이 없다")
        void doesNotCrossMidnight() {
            ShopBusinessHour businessHour = hour(LocalTime.of(9, 0), LocalTime.of(22, 0), false, false);

            assertThat(businessHour.extendsIntoNextDayAt(LocalTime.of(1, 0))).isFalse();
        }

        @Test
        @DisplayName("24시간·휴무 행은 연장 개념이 없다")
        void twentyFourHoursAndClosed() {
            assertThat(hour(null, null, false, true).extendsIntoNextDayAt(LocalTime.of(1, 0))).isFalse();
            assertThat(hour(LocalTime.of(20, 0), LocalTime.of(2, 0), true, false)
                .extendsIntoNextDayAt(LocalTime.of(1, 0))).isFalse();
        }

        @Test
        @DisplayName("개점·폐점 시각이 없으면 연장이 아니다")
        void nullTimes() {
            assertThat(hour(null, null, false, false).extendsIntoNextDayAt(LocalTime.of(1, 0))).isFalse();
        }
    }
}
