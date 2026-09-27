package com.tastyhouse.application.payment.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRefundStateMapperTest {

    @Test
    @DisplayName("PaymentRefund → PaymentRefundState → PaymentRefund 왕복 시 모든 필드가 보존된다")
    void paymentRefundRoundTrip() {
        PaymentRefund original = PaymentRefund.reconstitute(
            51L,
            PaymentId.of(52L),
            new Amount(7000),
            "부분 환불",
            RefundStatus.PROCESSING,
            "pg-refund-53",
            LocalDateTime.of(2026, 5, 2, 10, 1),
            LocalDateTime.of(2026, 5, 2, 10, 2)
        );

        PaymentRefund restored = PaymentRefundStateMapper.toDomain(PaymentRefundStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
