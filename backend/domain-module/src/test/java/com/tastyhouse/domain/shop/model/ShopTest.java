package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ShopTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 폐업하지 않은 상태다")
    void of_createsTransientShop() {
        Shop shop = Shop.of(
            StationId.of(1L),
            "상점명",
            BigDecimal.valueOf(37.5),
            BigDecimal.valueOf(127.0),
            "도로명 주소",
            "지번 주소",
            "010-1234-5678",
            UploadedFileId.of(10L)
        );

        assertThat(shop.getId()).isNull();
        assertThat(shop.getStationId()).isEqualTo(StationId.of(1L));
        assertThat(shop.getName()).isEqualTo("상점명");
        assertThat(shop.getLatitude()).isEqualTo(BigDecimal.valueOf(37.5));
        assertThat(shop.getLongitude()).isEqualTo(BigDecimal.valueOf(127.0));
        assertThat(shop.getRating()).isNull();
        assertThat(shop.getRoadAddress()).isEqualTo("도로명 주소");
        assertThat(shop.getLotAddress()).isEqualTo("지번 주소");
        assertThat(shop.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(shop.getThumbnailImageFileId()).isEqualTo(UploadedFileId.of(10L));
        assertThat(shop.isPermanentlyClosed()).isFalse();
        assertThat(shop.getCreatedAt()).isNull();
        assertThat(shop.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 상점 정보를 변경한다")
    void update_changesFields() {
        Shop shop = Shop.of(
            StationId.of(1L),
            "상점명",
            BigDecimal.valueOf(37.5),
            BigDecimal.valueOf(127.0),
            "도로명 주소",
            "지번 주소",
            "010-1234-5678",
            UploadedFileId.of(10L)
        );

        shop.update(
            StationId.of(2L),
            "새 상점명",
            BigDecimal.valueOf(37.6),
            BigDecimal.valueOf(127.1),
            "새 도로명 주소",
            "새 지번 주소",
            "010-9876-5432",
            UploadedFileId.of(20L)
        );

        assertThat(shop.getStationId()).isEqualTo(StationId.of(2L));
        assertThat(shop.getName()).isEqualTo("새 상점명");
        assertThat(shop.getLatitude()).isEqualTo(BigDecimal.valueOf(37.6));
        assertThat(shop.getLongitude()).isEqualTo(BigDecimal.valueOf(127.1));
        assertThat(shop.getRoadAddress()).isEqualTo("새 도로명 주소");
        assertThat(shop.getLotAddress()).isEqualTo("새 지번 주소");
        assertThat(shop.getPhoneNumber()).isEqualTo("010-9876-5432");
        assertThat(shop.getThumbnailImageFileId()).isEqualTo(UploadedFileId.of(20L));
    }

    @Test
    @DisplayName("close는 폐업 플래그를 true로 만든다")
    void close_marksPermanentlyClosed() {
        Shop shop = Shop.of(
            StationId.of(1L),
            "상점명",
            BigDecimal.valueOf(37.5),
            BigDecimal.valueOf(127.0),
            "도로명 주소",
            "지번 주소",
            "010-1234-5678",
            UploadedFileId.of(10L)
        );

        shop.close();

        assertThat(shop.isPermanentlyClosed()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·평점·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Shop shop = Shop.reconstitute(
            1L,
            CeoId.of(100L),
            StationId.of(2L),
            "상점명",
            BigDecimal.valueOf(37.5),
            BigDecimal.valueOf(127.0),
            4.5,
            "도로명 주소",
            "지번 주소",
            "010-1234-5678",
            UploadedFileId.of(10L),
            UploadedFileId.of(20L),
            true,
            false,
            false,
            10000,
            true,
            createdAt,
            updatedAt
        );

        assertThat(shop.getId()).isEqualTo(1L);
        assertThat(shop.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(shop.getRating()).isEqualTo(4.5);
        assertThat(shop.isPermanentlyClosed()).isTrue();
        assertThat(shop.getMinOrderAmount()).isEqualTo(10000);
        assertThat(shop.getCreatedAt()).isEqualTo(createdAt);
        assertThat(shop.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getShopId를 호출하면 ShopId 불변식 위반으로 예외가 발생한다")
    void getShopId_onTransient_throws() {
        Shop shop = Shop.of(
            StationId.of(1L),
            "상점명",
            BigDecimal.valueOf(37.5),
            BigDecimal.valueOf(127.0),
            "도로명 주소",
            "지번 주소",
            "010-1234-5678",
            UploadedFileId.of(10L)
        );

        assertThatThrownBy(shop::getShopId)
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Nested
    @DisplayName("폐업 가드")
    class PermanentClosureGuard {

        private Shop openShop() {
            return Shop.of(
                StationId.of(1L),
                "상점명",
                BigDecimal.valueOf(37.5),
                BigDecimal.valueOf(127.0),
                "도로명 주소",
                "지번 주소",
                "010-1234-5678",
                UploadedFileId.of(10L)
            );
        }

        private void update(Shop shop) {
            shop.update(
                StationId.of(2L),
                "새 상점명",
                BigDecimal.valueOf(37.6),
                BigDecimal.valueOf(127.1),
                "새 도로명",
                "새 지번",
                "010-9999-8888",
                UploadedFileId.of(20L)
            );
        }

        @Test
        @DisplayName("폐업 전에는 update·show가 정상 동작한다")
        void beforeClosure() {
            Shop shop = openShop();
            shop.hide();

            shop.show();
            update(shop);

            assertThat(shop.isHidden()).isFalse();
            assertThat(shop.getName()).isEqualTo("새 상점명");
        }

        @Test
        @DisplayName("폐업 후 update는 거부된다")
        void updateAfterClosure() {
            Shop shop = openShop();
            shop.close();

            assertThatThrownBy(() -> update(shop))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);

            // 거부된 update는 기존 상태를 바꾸지 않는다
            assertThat(shop.getName()).isEqualTo("상점명");
        }

        @Test
        @DisplayName("폐업 후 show(재노출)는 거부된다")
        void showAfterClosure() {
            Shop shop = openShop();
            shop.close();

            assertThatThrownBy(shop::show)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
        }

        @Test
        @DisplayName("폐업 후에도 hide(노출정지)와 close(멱등)는 허용된다")
        void hideAndCloseRemainAllowed() {
            Shop shop = openShop();
            shop.close();

            shop.hide();
            shop.close();

            assertThat(shop.isHidden()).isTrue();
            assertThat(shop.isPermanentlyClosed()).isTrue();
        }
    }

    @Nested
    @DisplayName("최소주문금액")
    class MinOrderAmount {

        private Shop shop() {
            return Shop.of(
                StationId.of(1L),
                "상점명",
                BigDecimal.valueOf(37.5),
                BigDecimal.valueOf(127.0),
                "도로명 주소",
                "지번 주소",
                "010-1234-5678",
                UploadedFileId.of(10L)
            );
        }

        private Shop shopWithMinOrderAmount() {
            Shop shop = shop();
            shop.changeMinOrderAmount(10000);
            return shop;
        }

        @Test
        @DisplayName("of로 생성한 가게는 최소주문금액이 미설정(0)이다")
        void of_startsUnset() {
            assertThat(shop().getMinOrderAmount()).isEqualTo(Shop.MIN_ORDER_AMOUNT_UNSET);
        }

        @ParameterizedTest
        @DisplayName("0(미설정)과 5,000~30,000 범위의 값은 설정할 수 있다")
        @ValueSource(ints = {0, 5000, 5001, 10000, 29999, 30000})
        void changeMinOrderAmount_acceptsValidValues(int amount) {
            Shop shop = shop();

            shop.changeMinOrderAmount(amount);

            assertThat(shop.getMinOrderAmount()).isEqualTo(amount);
        }

        @ParameterizedTest
        @DisplayName("0이 아니면서 5,000~30,000 범위를 벗어난 값은 거부된다")
        @ValueSource(ints = {-1, 1, 4999, 30001, 100000})
        void changeMinOrderAmount_rejectsOutOfRange(int amount) {
            Shop shop = shop();

            assertThatThrownBy(() -> shop.changeMinOrderAmount(amount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_MIN_ORDER_AMOUNT_OUT_OF_RANGE);

            // 거부된 변경은 기존 값을 바꾸지 않는다
            assertThat(shop.getMinOrderAmount()).isEqualTo(Shop.MIN_ORDER_AMOUNT_UNSET);
        }

        @Test
        @DisplayName("폐업한 가게의 최소주문금액은 변경할 수 없다")
        void changeMinOrderAmount_afterClosure_throws() {
            Shop shop = shopWithMinOrderAmount();
            shop.close();

            assertThatThrownBy(() -> shop.changeMinOrderAmount(20000))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);

            assertThat(shop.getMinOrderAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("배달 주문이 최소주문금액에 미달하면 거부된다")
        void validate_delivery_belowThreshold_throws() {
            Shop shop = shopWithMinOrderAmount();

            assertThatThrownBy(() -> shop.validateMinOrderAmount(OrderMethod.DELIVERY, 9999))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SHOP_MINIMUM_ORDER_AMOUNT_NOT_MET);
        }

        @ParameterizedTest
        @DisplayName("배달 주문이 최소주문금액 이상이면 통과한다(경계값 포함)")
        @ValueSource(ints = {10000, 10001, 50000})
        void validate_delivery_atOrAboveThreshold_passes(int amount) {
            Shop shop = shopWithMinOrderAmount();

            shop.validateMinOrderAmount(OrderMethod.DELIVERY, amount);
        }

        @ParameterizedTest
        @DisplayName("배달 외 주문방식은 금액과 무관하게 면제된다(픽업 포함)")
        @EnumSource(value = OrderMethod.class, names = {"TAKEOUT", "TABLE", "RESERVATION"})
        void validate_nonDelivery_isExempt(OrderMethod orderMethod) {
            Shop shop = shopWithMinOrderAmount();

            shop.validateMinOrderAmount(orderMethod, 1000);
        }

        @ParameterizedTest
        @DisplayName("최소주문금액이 미설정(0)이면 모든 주문방식·금액이 통과한다")
        @EnumSource(OrderMethod.class)
        void validate_whenUnset_passes(OrderMethod orderMethod) {
            Shop shop = shop();

            shop.validateMinOrderAmount(orderMethod, 0);
        }
    }
}
