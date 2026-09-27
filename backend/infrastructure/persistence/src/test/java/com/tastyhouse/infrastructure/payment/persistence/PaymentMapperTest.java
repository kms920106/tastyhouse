package com.tastyhouse.infrastructure.payment.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.payment.port.out.write.PaymentRefundState;
import com.tastyhouse.application.payment.port.out.write.PaymentState;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    @Test
    @DisplayName("PaymentState → 엔티티 → PaymentState 왕복 시 금액·PG사를 포함한 모든 필드가 보존된다")
    void paymentStateRoundTrip() {
        PaymentState state = new PaymentState(
            null,
            42L,
            "CREDIT_CARD",
            "COMPLETED",
            19500,
            "TOSS",
            "tid-43",
            "pg-order-44",
            "신한카드",
            "1234-****-****-5678",
            3,
            LocalDateTime.of(2026, 5, 1, 12, 1),
            LocalDateTime.of(2026, 5, 1, 12, 2),
            "고객 변심",
            "https://receipt.example/45",
            null
        );

        PaymentState restored = PaymentMapper.toState(PaymentMapper.toEntity(state));

        assertThat(restored).usingRecursiveComparison().isEqualTo(state);
    }

    @Test
    @DisplayName("PaymentRefundState → 엔티티 → PaymentRefundState 왕복 시 환불 금액·상태가 보존된다")
    void paymentRefundStateRoundTrip() {
        PaymentRefundState state = new PaymentRefundState(
            null,
            52L,
            7000,
            "부분 환불",
            "PROCESSING",
            "pg-refund-53",
            LocalDateTime.of(2026, 5, 2, 10, 1),
            null
        );

        PaymentRefundState restored = PaymentRefundMapper.toState(PaymentRefundMapper.toEntity(state));

        assertThat(restored).usingRecursiveComparison().isEqualTo(state);
    }
}
