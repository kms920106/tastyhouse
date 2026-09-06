package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class StorePriceBadgePolicyTest {
    private static final LocalDateTime SET_AT = LocalDateTime.of(2026, 3, 1, 15, 0);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 3, 10, 0);
    private static final Integer STORE_PRICE = 9000;

    private final StorePriceBadgePolicy policy = new StorePriceBadgePolicy();

    private static ProductPrice price(
        long id,
        long productId,
        Integer pickupPrice,
        LocalDateTime pickupPriceSetAt
    ) {
        return ProductPrice.reconstitute(
            id, ProductId.of(productId), null, 8000, STORE_PRICE, pickupPrice, 0,
            pickupPriceSetAt, null, null);
    }

    private static List<LocalDate> businessDaysAfterSetAt() {
        return List.of(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 3));
    }

    @Test
    @DisplayName("조건을 모두 만족하면 픽업 뱃지가 노출된다")
    void allConditionsMet_exposesBadge() {
        List<ProductPrice> prices = List.of(price(1L, 100L, 8500, SET_AT));

        assertThat(policy.shouldExposePickupBadge(prices, 1L, businessDaysAfterSetAt(), NOW)).isTrue();
    }

    @Test
    @DisplayName("픽업가가 매장가보다 높은 메뉴가 하나라도 있으면 노출하지 않는다")
    void anyPickupAboveStore_hidesBadge() {
        List<ProductPrice> prices = List.of(
            price(1L, 100L, 8500, SET_AT),
            price(2L, 101L, 9500, SET_AT)
        );

        assertThat(policy.shouldExposePickupBadge(prices, 2L, businessDaysAfterSetAt(), NOW)).isFalse();
    }

    @Test
    @DisplayName("커버리지가 80% 미만이면 노출하지 않는다")
    void belowCoverageThreshold_hidesBadge() {
        List<ProductPrice> prices = List.of(
            price(1L, 100L, 8500, SET_AT),
            price(2L, 101L, 8500, SET_AT),
            price(3L, 102L, 8500, SET_AT)
        );

        assertThat(policy.shouldExposePickupBadge(prices, 5L, businessDaysAfterSetAt(), NOW)).isFalse();
    }

    @Test
    @DisplayName("커버리지가 정확히 80%면 노출한다")
    void exactlyAtThreshold_exposesBadge() {
        List<ProductPrice> prices = List.of(
            price(1L, 100L, 8500, SET_AT),
            price(2L, 101L, 8500, SET_AT),
            price(3L, 102L, 8500, SET_AT),
            price(4L, 103L, 8500, SET_AT)
        );

        assertThat(policy.shouldExposePickupBadge(prices, 5L, businessDaysAfterSetAt(), NOW)).isTrue();
    }

    @Test
    @DisplayName("커버리지 분모는 가격 행이 아니라 메뉴 수다 — 한 메뉴의 여러 가격 행이 가중치를 갖지 않는다")
    void coverageCountsProductsNotRows() {
        List<ProductPrice> prices = List.of(
            price(1L, 100L, 8500, SET_AT),
            price(2L, 100L, 8500, SET_AT),
            price(3L, 100L, 8500, SET_AT),
            price(4L, 100L, 8500, SET_AT)
        );

        assertThat(policy.shouldExposePickupBadge(prices, 5L, businessDaysAfterSetAt(), NOW)).isFalse();
    }

    @Test
    @DisplayName("설정 당일에는 노출하지 않는다 — 익일(영업일) 규정")
    void sameDayAsSetAt_hidesBadge() {
        List<ProductPrice> prices = List.of(price(1L, 100L, 8500, SET_AT));

        List<LocalDate> sameDayOnly = List.of(LocalDate.of(2026, 3, 1));

        assertThat(policy.shouldExposePickupBadge(
            prices, 1L, sameDayOnly, SET_AT.plusHours(3))).isFalse();
    }

    @Test
    @DisplayName("설정 이후 영업일이 없으면(휴무만 이어지면) 노출하지 않는다")
    void noBusinessDayAfterSetAt_hidesBadge() {
        List<ProductPrice> prices = List.of(price(1L, 100L, 8500, SET_AT));

        assertThat(policy.shouldExposePickupBadge(prices, 1L, List.of(), NOW)).isFalse();
    }

    @Test
    @DisplayName("여러 메뉴 중 가장 늦게 설정된 픽업가를 기준으로 판정한다")
    void usesLatestPickupPriceSetAt() {
        List<ProductPrice> prices = List.of(
            price(1L, 100L, 8500, SET_AT),

            price(2L, 101L, 8500, NOW)
        );

        assertThat(policy.shouldExposePickupBadge(prices, 2L, businessDaysAfterSetAt(), NOW)).isFalse();
    }

    @Test
    @DisplayName("설정 시각을 모르는 과거 데이터는 익일 규정에서 제외한다 — 영구 미노출을 막는다")
    void unknownSetAt_isNotBlockedForever() {
        List<ProductPrice> prices = List.of(price(1L, 100L, 8500, null));

        assertThat(policy.shouldExposePickupBadge(prices, 1L, List.of(), NOW)).isTrue();
    }

    @Test
    @DisplayName("가격 행이 없거나 메뉴 수가 0이면 노출하지 않는다")
    void emptyInput_hidesBadge() {
        assertThat(policy.shouldExposePickupBadge(List.of(), 1L, businessDaysAfterSetAt(), NOW)).isFalse();
        assertThat(policy.shouldExposePickupBadge(
            List.of(price(1L, 100L, 8500, SET_AT)), 0L, businessDaysAfterSetAt(), NOW)).isFalse();
    }

    @Test
    @DisplayName("'매장과 같은 가격' 뱃지는 인증 플래그만 본다")
    void sameAsStorePriceBadge_followsVerificationFlag() {
        assertThat(policy.shouldExposeSameAsStorePriceBadge(true)).isTrue();
        assertThat(policy.shouldExposeSameAsStorePriceBadge(false)).isFalse();
    }
}
