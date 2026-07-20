package com.tastyhouse.core.domain.shop.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopOwnerMessageHistoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)다")
    void of_createsTransientMessageHistory() {
        ShopOwnerMessageHistory history = ShopOwnerMessageHistory.of(1L, "오늘도 감사합니다");

        assertThat(history.getId()).isNull();
        assertThat(history.getShopId()).isEqualTo(1L);
        assertThat(history.getMessage()).isEqualTo("오늘도 감사합니다");
        assertThat(history.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        ShopOwnerMessageHistory history = ShopOwnerMessageHistory.reconstitute(1L, 2L, "오늘도 감사합니다", createdAt);

        assertThat(history.getId()).isEqualTo(1L);
        assertThat(history.getShopId()).isEqualTo(2L);
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }
}
