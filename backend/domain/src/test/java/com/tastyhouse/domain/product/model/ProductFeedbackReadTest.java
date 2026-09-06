package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeedbackReadTest {
    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final LocalDateTime EARLIER = LocalDateTime.of(2026, 3, 1, 12, 0);
    private static final LocalDateTime LATER = LocalDateTime.of(2026, 3, 1, 13, 0);

    @Test
    @DisplayName("더 나중 시각이면 확인 시각을 밀어 올린다")
    void markRead_advancesToLaterTime() {
        ProductFeedbackRead feedbackRead = ProductFeedbackRead.of(SHOP_ID, EARLIER);

        feedbackRead.markRead(LATER);

        assertThat(feedbackRead.getReadAt()).isEqualTo(LATER);
    }

    @Test
    @DisplayName("과거 시각으로는 되돌리지 않는다 — 늦게 도착한 요청이 최신 확인을 덮으면 빨간 점이 다시 켜진다")
    void markRead_doesNotRewindToEarlierTime() {
        ProductFeedbackRead feedbackRead = ProductFeedbackRead.of(SHOP_ID, LATER);

        feedbackRead.markRead(EARLIER);

        assertThat(feedbackRead.getReadAt()).isEqualTo(LATER);
    }

    @Test
    @DisplayName("같은 시각이면 그대로 둔다")
    void markRead_sameTime_unchanged() {
        ProductFeedbackRead feedbackRead = ProductFeedbackRead.of(SHOP_ID, LATER);

        feedbackRead.markRead(LATER);

        assertThat(feedbackRead.getReadAt()).isEqualTo(LATER);
    }
}
