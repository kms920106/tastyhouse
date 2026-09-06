package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class PaymentRefundMapper {
    private PaymentRefundMapper() {
    }

    static PaymentRefund toDomain(PaymentRefundJpaEntity entity) {
        return PaymentRefund.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getPaymentId(), PaymentId::of),
            entity.getRefundAmount(),
            entity.getRefundReason(),
            entity.getRefundStatus(),
            entity.getPgRefundId(),
            entity.getRefundedAt(),
            entity.getCreatedAt()
        );
    }

    static PaymentRefundJpaEntity toEntity(PaymentRefund domain) {
        return PaymentRefundJpaEntity.create(
            IdMapping.raw(domain.getPaymentId(), PaymentId::value),
            domain.getRefundAmount(),
            domain.getRefundReason(),
            domain.getRefundStatus(),
            domain.getPgRefundId(),
            domain.getRefundedAt()
        );
    }
}
