package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.OrderMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 가격 행의 순수 단위 테스트.
 *
 * <p><b>주문유형 → 가격 해석이 이 테스트의 핵심이다.</b> 이 규칙이 화면과 어긋나면
 * {@code OrderPlacementService#validateAmounts}의 금액 대조가 실패해 <b>모든 주문이 거절</b>된다.
 * 그래서 네 가지 주문유형 전부와 픽업가 미설정 폴백을 명시적으로 못 박는다.
 */
class ProductPriceTest {

    private static final ProductId PRODUCT_ID = ProductId.of(1L);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 12, 0);

    private static ProductPrice price(Integer deliveryPrice, Integer storePrice, Integer pickupPrice) {
        return ProductPrice.reconstitute(
            1L, PRODUCT_ID, null, deliveryPrice, storePrice, pickupPrice, 0, null, null, null);
    }

    @Nested
    @DisplayName("주문유형별 가격 해석")
    class ResolvePrice {

        @Test
        @DisplayName("배달·테이블·예약은 배달가를 쓴다")
        void deliveryTableReservation_useDeliveryPrice() {
            ProductPrice price = price(10000, 9000, 8000);

            assertThat(price.resolvePrice(OrderMethod.DELIVERY)).isEqualTo(10000);
            assertThat(price.resolvePrice(OrderMethod.TABLE)).isEqualTo(10000);
            assertThat(price.resolvePrice(OrderMethod.RESERVATION)).isEqualTo(10000);
        }

        @Test
        @DisplayName("포장은 픽업가를 쓴다")
        void takeout_usesPickupPrice() {
            assertThat(price(10000, 9000, 8000).resolvePrice(OrderMethod.TAKEOUT)).isEqualTo(8000);
        }

        @Test
        @DisplayName("포장인데 픽업가가 없으면 배달가로 폴백한다")
        void takeout_withoutPickupPrice_fallsBackToDeliveryPrice() {
            assertThat(price(10000, 9000, null).resolvePrice(OrderMethod.TAKEOUT)).isEqualTo(10000);
        }

        @Test
        @DisplayName("매장가는 어떤 주문유형에서도 결제 가격이 되지 않는다(표시 전용)")
        void storePrice_isNeverUsedForPayment() {
            // 매장가만 유별나게 싼 상황 — 어느 주문유형도 이 값을 골라서는 안 된다.
            ProductPrice price = price(10000, 1000, null);

            for (OrderMethod orderMethod : OrderMethod.values()) {
                assertThat(price.resolvePrice(orderMethod)).isNotEqualTo(1000);
            }
        }
    }

    @Nested
    @DisplayName("가격 불변식")
    class Validation {

        @Test
        @DisplayName("음수 가격은 PRODUCT_PRICE_NEGATIVE로 거절된다")
        void negativePrice_isRejected() {
            assertThatThrownBy(() -> ProductPrice.of(PRODUCT_ID, null, -1, null, null, 0, NOW))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_NEGATIVE));

            assertThatThrownBy(() -> ProductPrice.of(PRODUCT_ID, null, 1000, -1, null, 0, NOW))
                .isInstanceOf(BusinessException.class);
            assertThatThrownBy(() -> ProductPrice.of(PRODUCT_ID, null, 1000, null, -1, 0, NOW))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("0원은 허용된다(0 이상이 규칙이다)")
        void zeroPrice_isAllowed() {
            assertThatCode(() -> ProductPrice.of(PRODUCT_ID, null, 0, 0, 0, 0, NOW))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("배달가는 필수다")
        void deliveryPrice_isRequired() {
            assertThatThrownBy(() -> ProductPrice.of(PRODUCT_ID, null, null, null, null, 0, NOW))
                .isInstanceOf(BusinessException.class)
                .satisfies(thrown -> assertThat(((BusinessException) thrown).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_PRICE_NEGATIVE));
        }

        @Test
        @DisplayName("배달가가 매장가보다 높아도 저장은 거부되지 않는다(인증이 풀리는 사유일 뿐)")
        void deliveryPriceAboveStorePrice_isNotRejectedOnSave() {
            assertThatCode(() -> ProductPrice.of(PRODUCT_ID, null, 10000, 9000, null, 0, NOW))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("reconstitute는 불변식을 검증하지 않는다(기존 위반 데이터도 로드된다)")
        void reconstitute_skipsValidation() {
            ProductPrice reconstituted = ProductPrice.reconstitute(
                1L, PRODUCT_ID, null, -5, -5, -5, 0, null, null, null);

            assertThat(reconstituted.getDeliveryPrice()).isEqualTo(-5);
        }
    }

    @Nested
    @DisplayName("인증 사유 판정")
    class UnverifiedReason {

        @Test
        @DisplayName("매장가가 없으면 미등록 사유가 우선한다")
        void noStorePrice_reportsNotRegistered() {
            assertThat(price(10000, null, null).resolveUnverifiedReason())
                .isEqualTo(StorePriceUnverifiedReason.STORE_PRICE_NOT_REGISTERED);
        }

        @Test
        @DisplayName("배달가가 매장가보다 높으면 그 사유를 돌려준다")
        void deliveryAboveStore_reportsHigherThanStore() {
            assertThat(price(10000, 9000, null).resolveUnverifiedReason())
                .isEqualTo(StorePriceUnverifiedReason.DELIVERY_PRICE_HIGHER_THAN_STORE);
        }

        @Test
        @DisplayName("배달가가 매장가 이하면 사유가 없다(null)")
        void deliveryWithinStore_hasNoReason() {
            assertThat(price(9000, 9000, null).resolveUnverifiedReason()).isNull();
            assertThat(price(8000, 9000, null).resolveUnverifiedReason()).isNull();
        }
    }

    @Nested
    @DisplayName("픽업가 설정 시각")
    class PickupPriceSetAt {

        @Test
        @DisplayName("픽업가가 같은 값으로 재전송되면 설정 시각이 밀리지 않는다")
        void unchangedPickupPrice_keepsSetAt() {
            LocalDateTime firstSetAt = NOW;
            ProductPrice price = ProductPrice.reconstitute(
                1L, PRODUCT_ID, null, 9000, 9000, 8000, 0, firstSetAt, null, null);

            price.change(null, 9000, 9000, 8000, 0, NOW.plusDays(3));

            // 전체 교체(PUT)에서 같은 값이 매번 재전송되므로, 여기서 시각이 밀리면 익일 노출 규정 때문에
            // 뱃지가 영구히 노출되지 않는다.
            assertThat(price.getPickupPriceSetAt()).isEqualTo(firstSetAt);
        }

        @Test
        @DisplayName("픽업가가 바뀌면 설정 시각이 갱신된다")
        void changedPickupPrice_refreshesSetAt() {
            ProductPrice price = ProductPrice.reconstitute(
                1L, PRODUCT_ID, null, 9000, 9000, 8000, 0, NOW, null, null);

            LocalDateTime later = NOW.plusDays(3);
            price.change(null, 9000, 9000, 7000, 0, later);

            assertThat(price.getPickupPriceSetAt()).isEqualTo(later);
        }

        @Test
        @DisplayName("픽업가를 비우면 설정 시각도 함께 비워진다")
        void clearedPickupPrice_clearsSetAt() {
            ProductPrice price = ProductPrice.reconstitute(
                1L, PRODUCT_ID, null, 9000, 9000, 8000, 0, NOW, null, null);

            price.change(null, 9000, 9000, null, 0, NOW.plusDays(1));

            // 껐다 켠 픽업가가 과거 시각을 근거로 즉시 노출되는 것을 막는다.
            assertThat(price.getPickupPriceSetAt()).isNull();
        }
    }

    @Nested
    @DisplayName("승인된 매장가 반영")
    class ApplyVerifiedStorePrice {

        @Test
        @DisplayName("픽업가 동일 설정이 켜지면 픽업가도 매장가와 같아진다")
        void applyPickupSamePrice_setsPickupPriceToStorePrice() {
            ProductPrice price = price(9000, null, null);

            price.applyVerifiedStorePrice(9000, true, NOW);

            assertThat(price.getStorePrice()).isEqualTo(9000);
            assertThat(price.getPickupPrice()).isEqualTo(9000);
            assertThat(price.getPickupPriceSetAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("픽업가 동일 설정이 꺼져 있으면 픽업가는 건드리지 않는다")
        void withoutApplyPickupSamePrice_leavesPickupPriceUntouched() {
            ProductPrice price = price(9000, null, null);

            price.applyVerifiedStorePrice(9000, false, NOW);

            assertThat(price.getStorePrice()).isEqualTo(9000);
            assertThat(price.getPickupPrice()).isNull();
            assertThat(price.getPickupPriceSetAt()).isNull();
        }
    }

    @Nested
    @DisplayName("뱃지 조건 술어")
    class BadgePredicates {

        @Test
        @DisplayName("매장가·픽업가가 모두 있어야 커버리지에 든다")
        void hasStoreAndPickupPrice() {
            assertThat(price(9000, 9000, 9000).hasStoreAndPickupPrice()).isTrue();
            assertThat(price(9000, 9000, null).hasStoreAndPickupPrice()).isFalse();
            assertThat(price(9000, null, 9000).hasStoreAndPickupPrice()).isFalse();
        }

        @Test
        @DisplayName("픽업가가 매장가 이하여야 픽업 뱃지 조건을 만족한다")
        void isPickupPriceWithinStorePrice() {
            assertThat(price(9000, 9000, 8000).isPickupPriceWithinStorePrice()).isTrue();
            assertThat(price(9000, 9000, 9000).isPickupPriceWithinStorePrice()).isTrue();
            assertThat(price(9000, 9000, 9500).isPickupPriceWithinStorePrice()).isFalse();
            // 값이 없으면 판정할 수 없으므로 false다.
            assertThat(price(9000, null, 8000).isPickupPriceWithinStorePrice()).isFalse();
        }

        @Test
        @DisplayName("매장가가 없으면 '배달가 초과'로 보지 않는다(미등록이라는 다른 사유다)")
        void isDeliveryPriceHigherThanStorePrice_requiresStorePrice() {
            assertThat(price(10000, null, null).isDeliveryPriceHigherThanStorePrice()).isFalse();
            assertThat(price(10000, 9000, null).isDeliveryPriceHigherThanStorePrice()).isTrue();
            assertThat(price(9000, 9000, null).isDeliveryPriceHigherThanStorePrice()).isFalse();
        }
    }
}
