package com.tastyhouse.core.domain.payment.domain.model;

import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PAYMENT_REFUND")
public class PaymentRefund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private PaymentId paymentId;

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

    private PaymentRefund(PaymentId paymentId, Amount refundAmount, String refundReason) {
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = RefundStatus.PENDING;
    }

    public static PaymentRefund create(PaymentId paymentId, Amount refundAmount, String refundReason) {
        return new PaymentRefund(paymentId, refundAmount, refundReason);
    }
}
