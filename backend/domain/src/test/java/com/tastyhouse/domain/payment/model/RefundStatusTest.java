package com.tastyhouse.domain.payment.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefundStatusTest {

    @Test
    @DisplayName("대기·처리 중·완료된 환불은 환불 가능 금액을 점유하고, 실패한 환불은 점유하지 않는다")
    void limitOccupyingStatuses_excludesFailed() {
        assertThat(RefundStatus.limitOccupyingStatuses())
            .containsExactlyInAnyOrder(RefundStatus.PENDING, RefundStatus.PROCESSING, RefundStatus.COMPLETED)
            .doesNotContain(RefundStatus.FAILED);
    }
}
