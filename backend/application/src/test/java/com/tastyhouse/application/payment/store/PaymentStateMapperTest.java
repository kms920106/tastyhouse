package com.tastyhouse.application.payment.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.Amount;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStateMapperTest {

    @Test
    @DisplayName("Payment → PaymentState → Payment 왕복 시 금액·PG사를 포함한 모든 필드가 보존된다")
    void paymentRoundTrip() {
        Payment original = Payment.reconstitute(
            41L,
            OrderId.of(42L),
            PaymentMethod.CREDIT_CARD,
            PaymentStatus.COMPLETED,
            new Amount(19500),
            PgProvider.TOSS,
            "tid-43",
            "pg-order-44",
            "신한카드",
            "1234-****-****-5678",
            3,
            LocalDateTime.of(2026, 5, 1, 12, 1),
            LocalDateTime.of(2026, 5, 1, 12, 2),
            "고객 변심",
            "https://receipt.example/45",
            LocalDateTime.of(2026, 5, 1, 12, 3)
        );

        Payment restored = PaymentStateMapper.toDomain(PaymentStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("PG사가 없는 현장 결제도 왕복에서 pgProvider가 null로 유지된다")
    void onSitePaymentWithoutPgProvider() {
        Payment original = Payment.reconstitute(
            41L, OrderId.of(42L), PaymentMethod.CASH_ON_SITE, PaymentStatus.PENDING, new Amount(0), null,
            null, null, null, null, null, null, null, null, null, LocalDateTime.of(2026, 5, 1, 12, 3));

        Payment restored = PaymentStateMapper.toDomain(PaymentStateMapper.toState(original));

        assertThat(restored.getPgProvider()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
