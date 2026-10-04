package com.tastyhouse.infrastructure.persistence.payment.persistence;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;

final class PaymentRefundMapper {

    private PaymentRefundMapper() {
    }

    static PaymentRefund toDomain(PaymentRefundJpaEntity entity) {
        return PaymentRefund.reconstitute(
            entity.getId(),
            entity.getPaymentId() == null ? null : PaymentId.of(entity.getPaymentId()),
            entity.getRefundAmount() == null ? null : new Amount(entity.getRefundAmount()),
            entity.getRefundReason(),
            entity.getRefundStatus() == null ? null : RefundStatus.valueOf(entity.getRefundStatus()),
            entity.getPgRefundId(),
            entity.getRefundedAt(),
            entity.getCreatedAt()
        );
    }

    static PaymentRefundJpaEntity toEntity(PaymentRefund paymentRefund) {
        return PaymentRefundJpaEntity.create(
            paymentRefund.getPaymentId() == null ? null : paymentRefund.getPaymentId().value(),
            paymentRefund.getRefundAmount() == null ? null : paymentRefund.getRefundAmount().value(),
            paymentRefund.getRefundReason(),
            paymentRefund.getRefundStatus() == null ? null : paymentRefund.getRefundStatus().name(),
            paymentRefund.getPgRefundId(),
            paymentRefund.getRefundedAt()
        );
    }
}
