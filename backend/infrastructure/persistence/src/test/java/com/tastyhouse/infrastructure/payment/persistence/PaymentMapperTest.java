package com.tastyhouse.infrastructure.payment.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    @Test
    @DisplayName("Payment → 엔티티 변환 시 금액·PG사를 포함한 모든 컬럼 값이 보존된다")
    void paymentToEntity() {
        PaymentJpaEntity entity = PaymentMapper.toEntity(payment());

        assertThat(entity.getOrderId()).isEqualTo(42L);
        assertThat(entity.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(entity.getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(entity.getAmount()).isEqualTo(19500);
        assertThat(entity.getPgProvider()).isEqualTo("TOSS");
        assertThat(entity.getPgTid()).isEqualTo("tid-43");
        assertThat(entity.getPgOrderId()).isEqualTo("pg-order-44");
        assertThat(entity.getCardCompany()).isEqualTo("신한카드");
        assertThat(entity.getCardNumber()).isEqualTo("1234-****-****-5678");
        assertThat(entity.getInstallmentMonths()).isEqualTo(3);
        assertThat(entity.getApprovedAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 1));
        assertThat(entity.getCancelledAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 2));
        assertThat(entity.getCancelReason()).isEqualTo("고객 변심");
        assertThat(entity.getReceiptUrl()).isEqualTo("https://receipt.example/45");
    }

    @Test
    @DisplayName("엔티티 → Payment 변환 시 id·생성 시각을 포함한 모든 필드가 보존된다")
    void paymentToDomain() {
        Payment original = payment();
        PaymentJpaEntity entity = PaymentMapper.toEntity(original);
        setAuditFields(entity, 41L, LocalDateTime.of(2026, 5, 1, 12, 3));

        Payment restored = PaymentMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("PG사가 없는 현장 결제는 엔티티의 pgProvider 컬럼이 null이다")
    void onSitePaymentToEntity() {
        PaymentJpaEntity entity = PaymentMapper.toEntity(onSitePayment());

        assertThat(entity.getPaymentMethod()).isEqualTo("CASH_ON_SITE");
        assertThat(entity.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(entity.getAmount()).isEqualTo(0);
        assertThat(entity.getPgProvider()).isNull();
        assertThat(entity.getPgTid()).isNull();
    }

    @Test
    @DisplayName("pgProvider 컬럼이 null인 엔티티는 pgProvider가 null인 Payment로 변환된다")
    void onSitePaymentToDomain() {
        Payment original = onSitePayment();
        PaymentJpaEntity entity = PaymentMapper.toEntity(original);
        setAuditFields(entity, 41L, LocalDateTime.of(2026, 5, 1, 12, 3));

        Payment restored = PaymentMapper.toDomain(entity);

        assertThat(restored.getPgProvider()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("PaymentRefund → 엔티티 변환 시 환불 금액·상태를 포함한 모든 컬럼 값이 보존된다")
    void paymentRefundToEntity() {
        PaymentRefundJpaEntity entity = PaymentRefundMapper.toEntity(paymentRefund());

        assertThat(entity.getPaymentId()).isEqualTo(52L);
        assertThat(entity.getRefundAmount()).isEqualTo(7000);
        assertThat(entity.getRefundReason()).isEqualTo("부분 환불");
        assertThat(entity.getRefundStatus()).isEqualTo("PROCESSING");
        assertThat(entity.getPgRefundId()).isEqualTo("pg-refund-53");
        assertThat(entity.getRefundedAt()).isEqualTo(LocalDateTime.of(2026, 5, 2, 10, 1));
    }

    @Test
    @DisplayName("엔티티 → PaymentRefund 변환 시 id·생성 시각을 포함한 모든 필드가 보존된다")
    void paymentRefundToDomain() {
        PaymentRefund original = paymentRefund();
        PaymentRefundJpaEntity entity = PaymentRefundMapper.toEntity(original);
        setAuditFields(entity, 51L, LocalDateTime.of(2026, 5, 2, 10, 2));

        PaymentRefund restored = PaymentRefundMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static void setAuditFields(Object entity, Long id, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", createdAt);
        ReflectionTestUtils.setField(entity, "updatedAt", createdAt.plusMinutes(1));
    }

    private static Payment payment() {
        return Payment.reconstitute(
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
    }

    private static Payment onSitePayment() {
        return Payment.reconstitute(
            41L, OrderId.of(42L), PaymentMethod.CASH_ON_SITE, PaymentStatus.PENDING, new Amount(0), null,
            null, null, null, null, null, null, null, null, null, LocalDateTime.of(2026, 5, 1, 12, 3));
    }

    private static PaymentRefund paymentRefund() {
        return PaymentRefund.reconstitute(
            51L,
            PaymentId.of(52L),
            new Amount(7000),
            "부분 환불",
            RefundStatus.PROCESSING,
            "pg-refund-53",
            LocalDateTime.of(2026, 5, 2, 10, 1),
            LocalDateTime.of(2026, 5, 2, 10, 2)
        );
    }
}
