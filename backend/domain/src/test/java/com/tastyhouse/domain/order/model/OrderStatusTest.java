package com.tastyhouse.domain.order.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("조리가 시작됐거나 완료된 주문만 환불 요청을 허용한다")
    void allowsRefundRequest_trueForPreparingAndCompleted() {
        assertThat(OrderStatus.PREPARING.allowsRefundRequest()).isTrue();
        assertThat(OrderStatus.COMPLETED.allowsRefundRequest()).isTrue();
    }

    @Test
    @DisplayName("조리 시작 전 주문은 취소 경로를 쓰고, 취소된 주문은 환불 요청을 허용하지 않는다")
    void allowsRefundRequest_falseForPendingConfirmedCancelled() {
        assertThat(OrderStatus.PENDING.allowsRefundRequest()).isFalse();
        assertThat(OrderStatus.CONFIRMED.allowsRefundRequest()).isFalse();
        assertThat(OrderStatus.CANCELLED.allowsRefundRequest()).isFalse();
    }
}
