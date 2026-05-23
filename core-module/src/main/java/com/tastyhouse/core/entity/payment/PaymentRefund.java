package com.tastyhouse.core.entity.payment;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "payment_id", nullable = false)
    private Long paymentId; // 결제 ID (PAYMENT.id 참조)

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount; // 환불 금액

    @Column(name = "refund_reason", length = 500)
    private String refundReason; // 환불 사유

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RefundStatus refundStatus; // 환불 상태 (PENDING: 대기, COMPLETED: 완료, FAILED: 실패)

    @Column(name = "pg_refund_id", length = 100)
    private String pgRefundId; // PG사 환불 거래 ID

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt; // 환불 완료 일시

    private PaymentRefund(
        Long paymentId,
        Integer refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String pgRefundId,
        LocalDateTime refundedAt
    ) {
        this.paymentId = paymentId;
        this.refundAmount = refundAmount != null ? refundAmount : 0;
        this.refundReason = refundReason;
        this.refundStatus = refundStatus != null ? refundStatus : RefundStatus.PENDING;
        this.pgRefundId = pgRefundId;
        this.refundedAt = refundedAt;
    }

    public static PaymentRefund of(
        Long paymentId,
        Integer refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String pgRefundId,
        LocalDateTime refundedAt
    ) {
        return new PaymentRefund(
            paymentId,
            refundAmount,
            refundReason,
            refundStatus,
            pgRefundId,
            refundedAt
        );
    }
}
