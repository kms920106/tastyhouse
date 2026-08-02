package com.tastyhouse.infrastructure.payment.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.payment.domain.model.RefundStatus;
import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 결제 환불 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code PaymentRefund}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PaymentRefundMapper}가 수행한다.
 * update 경로가 없는 insert 전용 애그리거트라 {@code applyChanges}는 두지 않는다.
 */
@Entity
@Table(name = "PAYMENT_REFUND")
public class PaymentRefundJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = PaymentIdConverter.class)
    @Column(name = "payment_id", nullable = false)
    private PaymentId paymentId;

    @Convert(converter = AmountConverter.class)
    @Column(name = "refund_amount", nullable = false)
    private Amount refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RefundStatus refundStatus;

    @Column(name = "pg_refund_id", length = 100)
    private String pgRefundId;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    protected PaymentRefundJpaEntity() {
    }

    private PaymentRefundJpaEntity(
        PaymentId paymentId,
        Amount refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String pgRefundId,
        LocalDateTime refundedAt
    ) {
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = refundStatus;
        this.pgRefundId = pgRefundId;
        this.refundedAt = refundedAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PaymentRefundMapper#toEntity}에서만 호출한다.
     */
    static PaymentRefundJpaEntity create(
        PaymentId paymentId,
        Amount refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String pgRefundId,
        LocalDateTime refundedAt
    ) {
        return new PaymentRefundJpaEntity(paymentId, refundAmount, refundReason, refundStatus, pgRefundId, refundedAt);
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
}
