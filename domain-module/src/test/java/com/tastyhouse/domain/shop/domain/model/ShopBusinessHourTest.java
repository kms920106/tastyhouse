package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopBusinessHourTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBusinessHour() {
        ShopBusinessHour businessHour = ShopBusinessHour.of(1L, DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false);

        assertThat(businessHour.getId()).isNull();
        assertThat(businessHour.getShopId()).isEqualTo(1L);
        assertThat(businessHour.getDayType()).isEqualTo(DayType.WEEKDAY);
        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(businessHour.getIsClosed()).isFalse();
        assertThat(businessHour.getIs24Hours()).isFalse();
    }

    @Test
    @DisplayName("update는 영업시간 정보를 변경한다")
    void update_changesFields() {
        ShopBusinessHour businessHour = ShopBusinessHour.of(1L, DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false);

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
            1L, 2L, DayType.WEEKDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false
        );

        assertThat(businessHour.getId()).isEqualTo(1L);
        assertThat(businessHour.getShopId()).isEqualTo(2L);
    }

    private static ShopBusinessHour hourOf(LocalTime openTime, LocalTime closeTime) {
        return ShopBusinessHour.of(1L, DayType.WEEKDAY, openTime, closeTime, false, false);
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
        assertThat(ShopBusinessHour.of(1L, DayType.WEEKDAY, null, null, true, false).getIsClosed()).isTrue();
        assertThat(ShopBusinessHour.of(1L, DayType.WEEKDAY, null, null, false, true).getIs24Hours()).isTrue();
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
            1L, 2L, DayType.WEEKDAY, LocalTime.of(9, 3), LocalTime.of(9, 33), false, false
        );

        assertThat(businessHour.getOpenTime()).isEqualTo(LocalTime.of(9, 3));
        assertThat(businessHour.getCloseTime()).isEqualTo(LocalTime.of(9, 33));
    }
}
