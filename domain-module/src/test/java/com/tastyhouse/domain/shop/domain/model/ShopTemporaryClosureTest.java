package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ShopTemporaryClosureTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사 시각 없음)이고 기간을 담는다")
    void of_createsTransientShopTemporaryClosure() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);

        ShopTemporaryClosure shopTemporaryClosure = ShopTemporaryClosure.of(ShopId.of(1L), startDate, endDate);

        assertThat(shopTemporaryClosure.getId()).isNull();
        assertThat(shopTemporaryClosure.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(shopTemporaryClosure.getStartDate()).isEqualTo(startDate);
        assertThat(shopTemporaryClosure.getEndDate()).isEqualTo(endDate);
        assertThat(shopTemporaryClosure.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("종료일이 시작일보다 이전이면 예외가 발생한다")
    void of_throwsException_whenEndDateBeforeStartDate() {
        LocalDate startDate = LocalDate.of(2026, 8, 3);
        LocalDate endDate = LocalDate.of(2026, 8, 1);

        assertThatThrownBy(() -> ShopTemporaryClosure.of(ShopId.of(1L), startDate, endDate))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_TEMPORARY_CLOSURE_INVALID_PERIOD);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사 시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 3);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 25, 10, 0);

        ShopTemporaryClosure shopTemporaryClosure = ShopTemporaryClosure.reconstitute(1L, ShopId.of(2L), startDate, endDate, createdAt);

        assertThat(shopTemporaryClosure.getId()).isEqualTo(1L);
        assertThat(shopTemporaryClosure.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(shopTemporaryClosure.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("days()는 시작일과 종료일을 포함한 총 일수를 계산한다")
    void days_calculatesInclusiveTotalDays() {
        ShopTemporaryClosure shopTemporaryClosure = ShopTemporaryClosure.of(
            ShopId.of(1L),
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 3)
        );

        assertThat(shopTemporaryClosure.days()).isEqualTo(3);
    }

    @Test
    @DisplayName("시작일과 종료일이 같으면 days()는 1이다")
    void days_returnsOne_whenStartDateEqualsEndDate() {
        LocalDate date = LocalDate.of(2026, 8, 1);

        ShopTemporaryClosure shopTemporaryClosure = ShopTemporaryClosure.of(ShopId.of(1L), date, date);

        assertThat(shopTemporaryClosure.days()).isEqualTo(1);
    }
}
