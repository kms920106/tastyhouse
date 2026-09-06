package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class PaymentMapper {
    private PaymentMapper() {
    }

    static Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            entity.getPaymentMethod(),
            entity.getPaymentStatus(),
            entity.getAmount(),
            entity.getPgProvider(),
            entity.getPgTid(),
            entity.getPgOrderId(),
            entity.getCardCompany(),
            entity.getCardNumber(),
            entity.getInstallmentMonths(),
            entity.getApprovedAt(),
            entity.getCancelledAt(),
            entity.getCancelReason(),
            entity.getReceiptUrl(),
            entity.getCreatedAt()
        );
    }

    static PaymentJpaEntity toEntity(Payment domain) {
        return PaymentJpaEntity.create(
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            domain.getPaymentMethod(),
            domain.getPaymentStatus(),
            domain.getAmount(),
            domain.getPgProvider(),
            domain.getPgTid(),
            domain.getPgOrderId(),
            domain.getCardCompany(),
            domain.getCardNumber(),
            domain.getInstallmentMonths(),
            domain.getApprovedAt(),
            domain.getCancelledAt(),
            domain.getCancelReason(),
            domain.getReceiptUrl()
        );
    }

    static void applyChanges(PaymentJpaEntity entity, Payment domain) {
        entity.applyChanges(
            domain.getPaymentStatus(),
            domain.getPgProvider(),
            domain.getPgTid(),
            domain.getPgOrderId(),
            domain.getCardCompany(),
            domain.getCardNumber(),
            domain.getInstallmentMonths(),
            domain.getApprovedAt(),
            domain.getCancelledAt(),
            domain.getCancelReason(),
            domain.getReceiptUrl()
        );
    }
}
