package com.tastyhouse.domain.payment.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PgOrderId;
import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class PaymentTest {

    private static final OrderId ORDER_ID = OrderId.of(1L);

    private Payment newPayment() {
        return Payment.create(ORDER_ID, PaymentMethod.CREDIT_CARD, new Amount(10000), PgOrderId.generate());
    }

    @Test
    @DisplayName("create로 생성하면 미영속 상태이고 paymentStatus는 PENDING이다")
    void create_createsTransientPaymentWithPendingStatus() {
        Payment payment = newPayment();

        assertThat(payment.getId()).isNull();
        assertThat(payment.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getAmount()).isEqualTo(new Amount(10000));
        assertThat(payment.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("complete는 PENDING 상태에서만 허용되고, 이후 COMPLETED로 전이한다")
    void complete_transitionsToCompletedOnlyFromPending() {
        Payment payment = newPayment();
        LocalDateTime approvedAt = LocalDateTime.now();

        payment.complete("PG-TID-1", approvedAt, "https://receipt.example.com/1");

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPgTid()).isEqualTo("PG-TID-1");
        assertThat(payment.getApprovedAt()).isEqualTo(approvedAt);
        assertThat(payment.getReceiptUrl()).isEqualTo("https://receipt.example.com/1");
    }

    @Test
    @DisplayName("이미 완료된 결제를 다시 complete하면 PAYMENT_NOT_PENDING 예외가 발생한다")
    void complete_throwsWhenNotPending() {
        Payment payment = newPayment();
        payment.complete("PG-TID-1", LocalDateTime.now(), null);

        assertThatThrownBy(() -> payment.complete("PG-TID-2", LocalDateTime.now(), null))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("fail은 paymentStatus를 FAILED로 전이한다")
    void fail_transitionsToFailed() {
        Payment payment = newPayment();

        payment.fail();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("cancel은 paymentStatus를 CANCELLED로 전이하고 취소 사유·시각을 기록한다")
    void cancel_transitionsToCancelled() {
        Payment payment = newPayment();
        LocalDateTime now = LocalDateTime.now();

        payment.cancel("고객 변심", now);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCancelledAt()).isEqualTo(now);
        assertThat(payment.getCancelReason()).isEqualTo("고객 변심");
    }

    @Test
    @DisplayName("updatePgInfo/updateCardInfo는 각각 PG·카드 정보를 갱신한다")
    void updateInfo_updatesFields() {
        Payment payment = newPayment();

        payment.updatePgInfo(PgProvider.TOSS, "TID-1", "PG-ORDER-1");
        payment.updateCardInfo("신한카드", "1234-****-****-5678", 3);

        assertThat(payment.getPgProvider()).isEqualTo(PgProvider.TOSS);
        assertThat(payment.getPgTid()).isEqualTo("TID-1");
        assertThat(payment.getPgOrderId()).isEqualTo("PG-ORDER-1");
        assertThat(payment.getCardCompany()).isEqualTo("신한카드");
        assertThat(payment.getCardNumber()).isEqualTo("1234-****-****-5678");
        assertThat(payment.getInstallmentMonths()).isEqualTo(3);
    }

    @Test
    @DisplayName("reconstitute는 식별자·감사 시각을 포함해 도메인 객체를 복원한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime approvedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Payment payment = Payment.reconstitute(
            100L,
            ORDER_ID,
            PaymentMethod.CREDIT_CARD,
            PaymentStatus.COMPLETED,
            new Amount(5000),
            PgProvider.TOSS,
            "TID-1",
            "PG-ORDER-1",
            "신한카드",
            "1234",
            0,
            approvedAt,
            null,
            null,
            "https://receipt.example.com",
            createdAt
        );

        assertThat(payment.getId()).isEqualTo(100L);
        assertThat(payment.getPaymentId().value()).isEqualTo(100L);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getCreatedAt()).isEqualTo(createdAt);
    }
}
