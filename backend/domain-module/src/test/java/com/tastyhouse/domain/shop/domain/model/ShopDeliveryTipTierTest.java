package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 구간별 기본 배달팁 한 행의 값 불변식 단위 테스트.
 *
 * <p>집합 관계 불변식(개수·정렬·단조성)은 {@code ShopDeliveryTipService}가 담당하므로 여기서는
 * 행 하나만 보고 판정할 수 있는 규칙(팁 범위·하한 금액·순서 범위)과 {@code covers} 경계만 본다.
 */
class ShopDeliveryTipTierTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);

    @Nested
    @DisplayName("tipAmount")
    class TipAmount {

        @ParameterizedTest(name = "배달팁 {0}원은 통과한다")
        @ValueSource(ints = {0, 1, 2500, 4999})
        @DisplayName("0원 이상 5,000원 미만 배달팁은 허용한다")
        void of_allowsTipBelowUpperBound(int tipAmount) {
            ShopDeliveryTipTier tier = ShopDeliveryTipTier.of(SHOP_ID, 0, 5000, tipAmount);

            assertThat(tier.getTipAmount()).isEqualTo(tipAmount);
        }

        @Test
        @DisplayName("5,000원 자체는 상한 미포함이라 SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE로 거부한다")
        void of_rejectsTipAtUpperBound() {
            assertThatThrownBy(() -> ShopDeliveryTipTier.of(SHOP_ID, 0, 5000, 5000))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("음수 배달팁은 SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE로 거부한다")
        void of_rejectsNegativeTip() {
            assertThatThrownBy(() -> ShopDeliveryTipTier.of(SHOP_ID, 0, 5000, -1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("minOrderAmount")
    class MinOrderAmount {

        @Test
        @DisplayName("구간 하한 주문금액 0원은 허용한다")
        void of_allowsZeroMinOrderAmount() {
            ShopDeliveryTipTier tier = ShopDeliveryTipTier.of(SHOP_ID, 0, 0, 2000);

            assertThat(tier.getMinOrderAmount()).isZero();
        }

        @Test
        @DisplayName("음수 구간 하한 주문금액은 SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE로 거부한다")
        void of_rejectsNegativeMinOrderAmount() {
            assertThatThrownBy(() -> ShopDeliveryTipTier.of(SHOP_ID, 0, -1, 2000))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("tierOrder")
    class TierOrder {

        @ParameterizedTest(name = "구간 순서 {0}은 통과한다")
        @ValueSource(ints = {0, 1, 2})
        @DisplayName("구간 순서 0~2(최대 3구간)는 허용한다")
        void of_allowsTierOrderWithinLimit(int tierOrder) {
            ShopDeliveryTipTier tier = ShopDeliveryTipTier.of(SHOP_ID, tierOrder, 5000, 2000);

            assertThat(tier.getTierOrder()).isEqualTo(tierOrder);
        }

        @ParameterizedTest(name = "구간 순서 {0}은 거부한다")
        @ValueSource(ints = {3, -1})
        @DisplayName("구간 순서가 0~2를 벗어나면 SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED로 거부한다")
        void of_rejectsTierOrderOutOfRange(int tierOrder) {
            assertThatThrownBy(() -> ShopDeliveryTipTier.of(SHOP_ID, tierOrder, 5000, 2000))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("covers")
    class Covers {

        @Test
        @DisplayName("주문금액이 구간 하한 미만이면 적용되지 않는다")
        void covers_falseBelowMinOrderAmount() {
            ShopDeliveryTipTier tier = ShopDeliveryTipTier.of(SHOP_ID, 0, 10000, 1500);

            assertThat(tier.covers(9999)).isFalse();
        }

        @Test
        @DisplayName("주문금액이 구간 하한과 같으면 적용된다(하한 포함)")
        void covers_trueAtMinOrderAmount() {
            ShopDeliveryTipTier tier = ShopDeliveryTipTier.of(SHOP_ID, 0, 10000, 1500);

            assertThat(tier.covers(10000)).isTrue();
        }

        @Test
        @DisplayName("주문금액이 구간 하한을 초과하면 적용된다")
        void covers_trueAboveMinOrderAmount() {
            ShopDeliveryTipTier tier = ShopDeliveryTipTier.of(SHOP_ID, 0, 10000, 1500);

            assertThat(tier.covers(10001)).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("검증하지 않는다 — 불변식을 위반한 기존 행도 로드할 수 있다")
        void reconstitute_bypassesValidation() {
            assertThatCode(() -> ShopDeliveryTipTier.reconstitute(1L, SHOP_ID, 9, -100, 99999))
                .doesNotThrowAnyException();

            ShopDeliveryTipTier tier = ShopDeliveryTipTier.reconstitute(1L, SHOP_ID, 9, -100, 99999);

            assertThat(tier.getId()).isEqualTo(1L);
            assertThat(tier.getTierOrder()).isEqualTo(9);
            assertThat(tier.getMinOrderAmount()).isEqualTo(-100);
            assertThat(tier.getTipAmount()).isEqualTo(99999);
        }
    }
}
