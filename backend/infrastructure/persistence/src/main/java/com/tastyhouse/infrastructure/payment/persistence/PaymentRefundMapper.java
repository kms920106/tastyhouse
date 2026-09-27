package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.application.payment.port.out.write.PaymentRefundState;

final class PaymentRefundMapper {
    private PaymentRefundMapper() {
    }

    static PaymentRefundState toState(PaymentRefundJpaEntity entity) {
        return new PaymentRefundState(
            entity.getId(),
            entity.getPaymentId(),
            entity.getRefundAmount(),
            entity.getRefundReason(),
            entity.getRefundStatus(),
            entity.getPgRefundId(),
            entity.getRefundedAt(),
            entity.getCreatedAt()
        );
    }

    static PaymentRefundJpaEntity toEntity(PaymentRefundState state) {
        return PaymentRefundJpaEntity.create(
            state.paymentId(),
            state.refundAmount(),
            state.refundReason(),
            state.refundStatus(),
            state.pgRefundId(),
            state.refundedAt()
        );
    }
}
