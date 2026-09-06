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

import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PAYMENT_REFUND")
public class PaymentRefundJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

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
        Long paymentId,
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

    static PaymentRefundJpaEntity create(
        Long paymentId,
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

    public Long getPaymentId() {
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
