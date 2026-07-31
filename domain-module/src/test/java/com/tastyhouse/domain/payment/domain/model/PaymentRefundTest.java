package com.tastyhouse.domain.payment.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class PaymentRefundTest {

    private static final PaymentId PAYMENT_ID = PaymentId.of(1L);

    @Test
    @DisplayName("create로 생성하면 미영속 상태이고 refundStatus는 PENDING이다")
    void create_createsTransientRefundWithPendingStatus() {
        PaymentRefund refund = PaymentRefund.create(PAYMENT_ID, new Amount(3000), "단순 변심");

        assertThat(refund.getId()).isNull();
        assertThat(refund.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(refund.getRefundAmount()).isEqualTo(new Amount(3000));
        assertThat(refund.getRefundReason()).isEqualTo("단순 변심");
        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.PENDING);
        assertThat(refund.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 식별자·감사 시각을 포함해 도메인 객체를 복원한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime refundedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        PaymentRefund refund = PaymentRefund.reconstitute(
            10L,
            PAYMENT_ID,
            new Amount(3000),
            "단순 변심",
            RefundStatus.COMPLETED,
            "PG-REFUND-1",
            refundedAt,
            createdAt
        );

        assertThat(refund.getId()).isEqualTo(10L);
        assertThat(refund.getPaymentRefundId().value()).isEqualTo(10L);
        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(refund.getPgRefundId()).isEqualTo("PG-REFUND-1");
        assertThat(refund.getRefundedAt()).isEqualTo(refundedAt);
        assertThat(refund.getCreatedAt()).isEqualTo(createdAt);
    }
}
