package com.tastyhouse.domain.shop.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ShopConvenienceInfoTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사 시각 없음)이고 편의정보를 담는다")
    void of_createsTransientShopConvenienceInfo() {
        ShopConvenienceInfo shopConvenienceInfo = ShopConvenienceInfo.of(
            1L, true, false, true, true, "1번 출구에서 직진 100m", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0)
        );

        assertThat(shopConvenienceInfo.getId()).isNull();
        assertThat(shopConvenienceInfo.getShopId()).isEqualTo(1L);
        assertThat(shopConvenienceInfo.isParkingAvailable()).isTrue();
        assertThat(shopConvenienceInfo.isParkingPaid()).isFalse();
        assertThat(shopConvenienceInfo.isValetAvailable()).isTrue();
        assertThat(shopConvenienceInfo.isValetPaid()).isTrue();
        assertThat(shopConvenienceInfo.getDirectionsGuide()).isEqualTo("1번 출구에서 직진 100m");
        assertThat(shopConvenienceInfo.getDisplayLatitude()).isEqualTo(BigDecimal.valueOf(37.5));
        assertThat(shopConvenienceInfo.getDisplayLongitude()).isEqualTo(BigDecimal.valueOf(127.0));
        assertThat(shopConvenienceInfo.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("찾아오는 길 안내가 200자를 초과하면 예외가 발생한다")
    void of_throwsException_whenDirectionsGuideExceedsMaxLength() {
        String tooLong = "가".repeat(201);

        assertThatThrownBy(() -> ShopConvenienceInfo.of(1L, true, false, true, true, tooLong, null, null))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DIRECTIONS_GUIDE_TOO_LONG);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사 시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 25, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 25, 11, 0);

        ShopConvenienceInfo shopConvenienceInfo = ShopConvenienceInfo.reconstitute(
            1L, 2L, true, false, true, true, "안내문구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), createdAt, updatedAt
        );

        assertThat(shopConvenienceInfo.getId()).isEqualTo(1L);
        assertThat(shopConvenienceInfo.getShopId()).isEqualTo(2L);
        assertThat(shopConvenienceInfo.getCreatedAt()).isEqualTo(createdAt);
        assertThat(shopConvenienceInfo.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("update는 편의정보 필드를 갱신한다")
    void update_changesFields() {
        ShopConvenienceInfo shopConvenienceInfo = ShopConvenienceInfo.of(
            1L, true, false, true, true, "이전 안내", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0)
        );

        shopConvenienceInfo.update(false, true, false, false, "새로운 안내", BigDecimal.valueOf(37.6), BigDecimal.valueOf(127.1));

        assertThat(shopConvenienceInfo.isParkingAvailable()).isFalse();
        assertThat(shopConvenienceInfo.isParkingPaid()).isTrue();
        assertThat(shopConvenienceInfo.isValetAvailable()).isFalse();
        assertThat(shopConvenienceInfo.isValetPaid()).isFalse();
        assertThat(shopConvenienceInfo.getDirectionsGuide()).isEqualTo("새로운 안내");
        assertThat(shopConvenienceInfo.getDisplayLatitude()).isEqualTo(BigDecimal.valueOf(37.6));
        assertThat(shopConvenienceInfo.getDisplayLongitude()).isEqualTo(BigDecimal.valueOf(127.1));
    }

    @Test
    @DisplayName("update 시 찾아오는 길 안내가 200자를 초과하면 예외가 발생한다")
    void update_throwsException_whenDirectionsGuideExceedsMaxLength() {
        ShopConvenienceInfo shopConvenienceInfo = ShopConvenienceInfo.of(1L, true, false, true, true, "안내", null, null);
        String tooLong = "가".repeat(201);

        assertThatThrownBy(() -> shopConvenienceInfo.update(true, false, true, true, tooLong, null, null))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DIRECTIONS_GUIDE_TOO_LONG);
    }
}
