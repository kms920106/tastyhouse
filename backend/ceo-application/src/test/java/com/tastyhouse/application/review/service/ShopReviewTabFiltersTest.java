package com.tastyhouse.application.review.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.application.review.port.out.ShopReviewTabFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopReviewTabFiltersTest {

    @Test
    @DisplayName("전체 탭은 조건이 없고 나머지 탭은 하나의 조건만 켠다")
    void tabFilters() {
        assertThat(ShopReviewTabFilters.of(ReviewListTab.ALL)).isEqualTo(new ShopReviewTabFilter(false, false, false));
        assertThat(ShopReviewTabFilters.of(ReviewListTab.UNANSWERED)).isEqualTo(new ShopReviewTabFilter(true, false, false));
        assertThat(ShopReviewTabFilters.of(ReviewListTab.BLINDED)).isEqualTo(new ShopReviewTabFilter(false, true, false));
        assertThat(ShopReviewTabFilters.of(ReviewListTab.OWNER_ONLY)).isEqualTo(new ShopReviewTabFilter(false, false, true));
    }

    @Test
    @DisplayName("탭 조건을 둘 이상 켜면 생성 시점에 거부한다 — DAO가 앞선 조건만 조용히 적용하는 것을 막는다")
    void rejectsMultipleTabs() {
        assertThatThrownBy(() -> new ShopReviewTabFilter(true, true, false))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
