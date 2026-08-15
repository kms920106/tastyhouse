package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ShopRiderGuideTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태이고 문구·픽업 위치가 모두 비어 있다")
    void of_createsEmptyTransientRiderGuide() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        assertThat(riderGuide.getId()).isNull();
        assertThat(riderGuide.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(riderGuide.getVisitGuide()).isNull();
        assertThat(riderGuide.hasPickupLocation()).isFalse();
        assertThat(riderGuide.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사 시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 8, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 8, 11, 0);

        ShopRiderGuide riderGuide = ShopRiderGuide.reconstitute(
            1L, ShopId.of(2L), "안내 문구", "서울시 강남구 테헤란로 1", "서울시 강남구 역삼동 1-1", "지하 1층 후문",
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), createdAt, updatedAt
        );

        assertThat(riderGuide.getId()).isEqualTo(1L);
        assertThat(riderGuide.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(riderGuide.getVisitGuide()).isEqualTo("안내 문구");
        assertThat(riderGuide.getPickupDetailAddress()).isEqualTo("지하 1층 후문");
        assertThat(riderGuide.hasPickupLocation()).isTrue();
        assertThat(riderGuide.getCreatedAt()).isEqualTo(createdAt);
        assertThat(riderGuide.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("안내 문구를 변경하면 새 문구로 대체된다")
    void changeVisitGuide_replacesVisitGuide() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        riderGuide.changeVisitGuide("OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.");

        assertThat(riderGuide.getVisitGuide()).isEqualTo("OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.");
    }

    @Test
    @DisplayName("빈 문자열·공백 문구는 null로 정규화된다(빈 값 PUT = 삭제)")
    void changeVisitGuide_normalizesBlankToNull() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));
        riderGuide.changeVisitGuide("등록된 문구");

        riderGuide.changeVisitGuide("   ");

        assertThat(riderGuide.getVisitGuide()).isNull();
    }

    @Test
    @DisplayName("안내 문구가 200자를 초과하면 예외가 발생한다")
    void changeVisitGuide_throwsException_whenTooLong() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));
        String tooLong = "가".repeat(201);

        assertThatThrownBy(() -> riderGuide.changeVisitGuide(tooLong))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_VISIT_GUIDE_TOO_LONG);
    }

    @Test
    @DisplayName("픽업 위치를 등록하면 주소·좌표가 담기고 설정된 상태가 된다")
    void changePickupLocation_setsPickupLocation() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", "서울시 강남구 역삼동 1-1", "지하 1층 후문",
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621)
        );

        assertThat(riderGuide.getPickupRoadAddress()).isEqualTo("서울시 강남구 테헤란로 1");
        assertThat(riderGuide.getPickupLotAddress()).isEqualTo("서울시 강남구 역삼동 1-1");
        assertThat(riderGuide.getPickupDetailAddress()).isEqualTo("지하 1층 후문");
        assertThat(riderGuide.getPickupLatitude()).isEqualTo(BigDecimal.valueOf(37.497942));
        assertThat(riderGuide.getPickupLongitude()).isEqualTo(BigDecimal.valueOf(127.027621));
        assertThat(riderGuide.hasPickupLocation()).isTrue();
    }

    @Test
    @DisplayName("위경도 중 하나만 채우면 예외가 발생한다(전부 채우거나 전부 비우거나)")
    void changePickupLocation_throwsException_whenPartiallyFilled() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        assertThatThrownBy(() -> riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", null, null, BigDecimal.valueOf(37.497942), null
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_PICKUP_LOCATION_INCOMPLETE);
    }

    @Test
    @DisplayName("좌표 없이 주소만 채우면 예외가 발생한다")
    void changePickupLocation_throwsException_whenCoordinatesMissing() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        assertThatThrownBy(() -> riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", null, null, null, null
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_PICKUP_LOCATION_INCOMPLETE);
    }

    @Test
    @DisplayName("전 필드를 비운 픽업 위치 변경은 허용된다")
    void changePickupLocation_allowsAllEmpty() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        riderGuide.changePickupLocation(null, null, null, null, null);

        assertThat(riderGuide.hasPickupLocation()).isFalse();
    }

    @Test
    @DisplayName("위도가 범위를 벗어나면 예외가 발생한다")
    void changePickupLocation_throwsException_whenLatitudeOutOfRange() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        assertThatThrownBy(() -> riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", null, null, BigDecimal.valueOf(91), BigDecimal.valueOf(127.027621)
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_PICKUP_LOCATION_INVALID);
    }

    @Test
    @DisplayName("경도가 범위를 벗어나면 예외가 발생한다")
    void changePickupLocation_throwsException_whenLongitudeOutOfRange() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));

        assertThatThrownBy(() -> riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", null, null, BigDecimal.valueOf(37.497942), BigDecimal.valueOf(181)
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_PICKUP_LOCATION_INVALID);
    }

    @Test
    @DisplayName("픽업 상세주소가 100자를 초과하면 예외가 발생한다")
    void changePickupLocation_throwsException_whenDetailAddressTooLong() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));
        String tooLong = "가".repeat(101);

        assertThatThrownBy(() -> riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", null, tooLong, BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621)
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_PICKUP_DETAIL_ADDRESS_TOO_LONG);
    }

    @Test
    @DisplayName("픽업 위치를 초기화하면 5개 필드가 모두 비고 문구는 유지된다")
    void clearPickupLocation_clearsOnlyPickupFields() {
        ShopRiderGuide riderGuide = ShopRiderGuide.of(ShopId.of(1L));
        riderGuide.changeVisitGuide("안내 문구");
        riderGuide.changePickupLocation(
            "서울시 강남구 테헤란로 1", "서울시 강남구 역삼동 1-1", "지하 1층 후문",
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621)
        );

        riderGuide.clearPickupLocation();

        assertThat(riderGuide.getPickupRoadAddress()).isNull();
        assertThat(riderGuide.getPickupLotAddress()).isNull();
        assertThat(riderGuide.getPickupDetailAddress()).isNull();
        assertThat(riderGuide.getPickupLatitude()).isNull();
        assertThat(riderGuide.getPickupLongitude()).isNull();
        assertThat(riderGuide.hasPickupLocation()).isFalse();
        assertThat(riderGuide.getVisitGuide()).isEqualTo("안내 문구");
    }
}
