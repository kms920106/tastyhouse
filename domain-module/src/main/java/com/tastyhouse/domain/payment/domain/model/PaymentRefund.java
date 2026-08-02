package com.tastyhouse.domain.payment.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.domain.payment.domain.vo.PaymentRefundId;

/**
 * 결제 환불 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PaymentRefundJpaEntity} + {@code PaymentRefundMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code PaymentRefundRepository#save}를 호출해야 한다.
 */
public class PaymentRefund {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final PaymentId paymentId; // 환불 대상 결제 ID
    private final Amount refundAmount; // 환불 금액
    private final String refundReason; // 환불 사유
    private final RefundStatus refundStatus; // 환불 상태
    private final String pgRefundId; // PG 환불 ID
    private final LocalDateTime refundedAt; // 환불 완료 시각
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private PaymentRefund(
        Long id,
        PaymentId paymentId,
        Amount refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String pgRefundId,
        LocalDateTime refundedAt,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = refundStatus;
        this.pgRefundId = pgRefundId;
        this.refundedAt = refundedAt;
        this.createdAt = createdAt;
    }

    /**
     * 신규 환불 요청을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static PaymentRefund create(PaymentId paymentId, Amount refundAmount, String refundReason) {
        return new PaymentRefund(null, paymentId, refundAmount, refundReason, RefundStatus.PENDING, null, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static PaymentRefund reconstitute(
        Long id,
        PaymentId paymentId,
        Amount refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String pgRefundId,
        LocalDateTime refundedAt,
        LocalDateTime createdAt
    ) {
        return new PaymentRefund(id, paymentId, refundAmount, refundReason, refundStatus, pgRefundId, refundedAt, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public PaymentId getPaymentId() {
        return this.paymentId;
    }

    public Amount getRefundAmount() {
        return this.refundAmount;
    }

    public String getRefundReason() {
        return this.refundReason;
    }

    public RefundStatus getRefundStatus() {
        return this.refundStatus;
    }

    public String getPgRefundId() {
        return this.pgRefundId;
    }

    public LocalDateTime getRefundedAt() {
        return this.refundedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public PaymentRefundId getPaymentRefundId() {
        return PaymentRefundId.of(this.id);
    }
}
