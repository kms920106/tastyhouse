package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 배달팁 설정 헤더(거리별 값과 추가 배달팁 방식의 소유자) 단위 테스트.
 *
 * <p>거리↔지역 배타 검증 자체는 다른 애그리거트 컬렉션을 읽어야 하므로 {@code ShopDeliveryTipService}가
 * 담당한다. 여기서는 헤더가 혼자 판정할 수 있는 값의 불변식과 할증 계산식만 본다.
 */
class ShopDeliveryTipSettingTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);

    @Nested
    @DisplayName("changeToDistance - 기본배달거리")
    class BaseDistance {

        @ParameterizedTest(name = "기본배달거리 {0}m는 통과한다")
        @ValueSource(ints = {1000, 1500, 2000, 2500, 3000})
        @DisplayName("허용값(1/1.5/2/2.5/3km)은 통과한다")
        void changeToDistance_allowsWhitelistedBaseDistance(int baseDistanceMeters) {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            setting.changeToDistance(baseDistanceMeters, DeliveryTipDistanceUnit.PER_500M, 500);

            assertThat(setting.getBaseDistanceMeters()).isEqualTo(baseDistanceMeters);
            assertThat(setting.getExtraTipType()).isEqualTo(DeliveryTipExtraType.DISTANCE);
        }

        @ParameterizedTest(name = "기본배달거리 {0}m는 거부한다")
        @ValueSource(ints = {1200, 3500, 0, 500})
        @DisplayName("허용값이 아니면 SHOP_DELIVERY_TIP_DISTANCE_BASE_INVALID로 거부한다")
        void changeToDistance_rejectsUnlistedBaseDistance(int baseDistanceMeters) {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            assertThatThrownBy(() -> setting.changeToDistance(baseDistanceMeters, DeliveryTipDistanceUnit.PER_500M, 500))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_BASE_INVALID);
        }
    }

    @Nested
    @DisplayName("changeToDistance - 단위별 할증 범위")
    class SurchargeRange {

        @ParameterizedTest(name = "PER_100M {0}원은 통과한다")
        @ValueSource(ints = {100, 200, 300})
        @DisplayName("PER_100M은 100~300원을 허용한다")
        void changeToDistance_allowsPer100mWithinRange(int surchargeAmount) {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            setting.changeToDistance(1000, DeliveryTipDistanceUnit.PER_100M, surchargeAmount);

            assertThat(setting.getSurchargeAmount()).isEqualTo(surchargeAmount);
        }

        @ParameterizedTest(name = "PER_100M {0}원은 거부한다")
        @ValueSource(ints = {99, 301})
        @DisplayName("PER_100M 범위를 벗어나면 SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE로 거부한다")
        void changeToDistance_rejectsPer100mOutOfRange(int surchargeAmount) {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            assertThatThrownBy(() -> setting.changeToDistance(1000, DeliveryTipDistanceUnit.PER_100M, surchargeAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE);
        }

        @ParameterizedTest(name = "PER_500M {0}원은 통과한다")
        @ValueSource(ints = {100, 500, 1500})
        @DisplayName("PER_500M은 100~1,500원을 허용한다")
        void changeToDistance_allowsPer500mWithinRange(int surchargeAmount) {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            setting.changeToDistance(1000, DeliveryTipDistanceUnit.PER_500M, surchargeAmount);

            assertThat(setting.getSurchargeAmount()).isEqualTo(surchargeAmount);
        }

        @ParameterizedTest(name = "PER_500M {0}원은 거부한다")
        @ValueSource(ints = {99, 1501})
        @DisplayName("PER_500M 범위를 벗어나면 SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE로 거부한다")
        void changeToDistance_rejectsPer500mOutOfRange(int surchargeAmount) {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            assertThatThrownBy(() -> setting.changeToDistance(1000, DeliveryTipDistanceUnit.PER_500M, surchargeAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_SURCHARGE_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("calculateDistanceSurcharge")
    class CalculateDistanceSurcharge {

        @Test
        @DisplayName("PDF 예시: 설정이 달라도 둘 다 3km 배달 시 기본팁 포함 4,000원이 된다")
        void calculateDistanceSurcharge_pdfGoldenCases() {
            // 설정1: 기본배달거리 1.5km / 기본팁 2,500원 + 500m당 500원
            ShopDeliveryTipSetting first = distanceSetting(1500, 500);
            int firstSurcharge = first.calculateDistanceSurcharge(3000);

            assertThat(firstSurcharge).isEqualTo(1500);
            assertThat(2500 + firstSurcharge).isEqualTo(4000);

            // 설정2: 기본배달거리 2km / 기본팁 2,000원 + 500m당 1,000원
            ShopDeliveryTipSetting second = distanceSetting(2000, 1000);
            int secondSurcharge = second.calculateDistanceSurcharge(3000);

            assertThat(secondSurcharge).isEqualTo(2000);
            assertThat(2000 + secondSurcharge).isEqualTo(4000);
        }

        @Test
        @DisplayName("기본배달거리 이내면 할증이 없다")
        void calculateDistanceSurcharge_zeroWithinBaseDistance() {
            ShopDeliveryTipSetting setting = distanceSetting(2000, 500);

            assertThat(setting.calculateDistanceSurcharge(1999)).isZero();
            assertThat(setting.calculateDistanceSurcharge(2000)).isZero();
        }

        @Test
        @DisplayName("초과분은 단위 거리로 올림한다 — 1m만 넘겨도 1단위를 부과한다")
        void calculateDistanceSurcharge_roundsUpToUnit() {
            ShopDeliveryTipSetting setting = distanceSetting(1000, 500);

            assertThat(setting.calculateDistanceSurcharge(1001)).isEqualTo(500);
            assertThat(setting.calculateDistanceSurcharge(1500)).isEqualTo(500);
            assertThat(setting.calculateDistanceSurcharge(1501)).isEqualTo(1000);
        }

        @Test
        @DisplayName("아무리 멀어도 추가 배달팁 상한 10,000원으로 절삭한다")
        void calculateDistanceSurcharge_cappedAtExtraTipUpperBound() {
            ShopDeliveryTipSetting setting = distanceSetting(1000, 1500);

            assertThat(setting.calculateDistanceSurcharge(100000)).isEqualTo(10000);
        }

        @Test
        @DisplayName("거리별 설정이 아니면 0원이다")
        void calculateDistanceSurcharge_zeroWhenNotDistanceType() {
            ShopDeliveryTipSetting none = ShopDeliveryTipSetting.of(SHOP_ID);
            assertThat(none.calculateDistanceSurcharge(9999)).isZero();

            ShopDeliveryTipSetting region = distanceSetting(1000, 500);
            region.changeToRegion();
            assertThat(region.calculateDistanceSurcharge(9999)).isZero();
        }
    }

    @Nested
    @DisplayName("전환 메서드")
    class Transition {

        @Test
        @DisplayName("of는 추가 배달팁 미사용(NONE) 상태로 시작한다")
        void of_startsWithNoExtraTip() {
            ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);

            assertThat(setting.getId()).isNull();
            assertThat(setting.getExtraTipType()).isEqualTo(DeliveryTipExtraType.NONE);
            assertThat(setting.usesDistance()).isFalse();
            assertThat(setting.usesRegion()).isFalse();
        }

        @Test
        @DisplayName("changeToRegion은 거리별 값 3개를 비워 배타성을 값 수준에서도 유지한다")
        void changeToRegion_clearsDistanceFields() {
            ShopDeliveryTipSetting setting = distanceSetting(1500, 500);

            setting.changeToRegion();

            assertThat(setting.getExtraTipType()).isEqualTo(DeliveryTipExtraType.REGION);
            assertThat(setting.getBaseDistanceMeters()).isNull();
            assertThat(setting.getSurchargeUnit()).isNull();
            assertThat(setting.getSurchargeAmount()).isNull();
            assertThat(setting.usesRegion()).isTrue();
            assertThat(setting.usesDistance()).isFalse();
        }

        @Test
        @DisplayName("clearExtraTip은 NONE으로 되돌리고 거리별 값 3개를 비운다")
        void clearExtraTip_resetsToNone() {
            ShopDeliveryTipSetting setting = distanceSetting(1500, 500);

            setting.clearExtraTip();

            assertThat(setting.getExtraTipType()).isEqualTo(DeliveryTipExtraType.NONE);
            assertThat(setting.getBaseDistanceMeters()).isNull();
            assertThat(setting.getSurchargeUnit()).isNull();
            assertThat(setting.getSurchargeAmount()).isNull();
            assertThat(setting.usesDistance()).isFalse();
            assertThat(setting.usesRegion()).isFalse();
        }
    }

    private static ShopDeliveryTipSetting distanceSetting(int baseDistanceMeters, int surchargeAmount) {
        ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);
        setting.changeToDistance(baseDistanceMeters, DeliveryTipDistanceUnit.PER_500M, surchargeAmount);
        return setting;
    }
}
