package com.tastyhouse.domain.payment.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;

public class PaymentRefund {
    private final Long id;
    private final PaymentId paymentId;
    private final Amount refundAmount;
    private final String refundReason;
    private final RefundStatus refundStatus;
    private final String pgRefundId;
    private final LocalDateTime refundedAt;
    private final LocalDateTime createdAt;

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

    public static PaymentRefund create(PaymentId paymentId, Amount refundAmount, String refundReason) {
        return new PaymentRefund(null, paymentId, refundAmount, refundReason, RefundStatus.PENDING, null, null, null);
    }

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
