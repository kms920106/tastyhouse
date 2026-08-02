package com.tastyhouse.domain.shop.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.domain.vo.CeoId;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.shop.domain.vo.StationId;
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
            createdAt,
            updatedAt
        );

        assertThat(shop.getId()).isEqualTo(1L);
        assertThat(shop.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(shop.getRating()).isEqualTo(4.5);
        assertThat(shop.isPermanentlyClosed()).isTrue();
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
}
