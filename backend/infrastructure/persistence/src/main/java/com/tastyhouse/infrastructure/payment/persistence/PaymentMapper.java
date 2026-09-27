package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.application.payment.port.out.write.PaymentState;

final class PaymentMapper {
    private PaymentMapper() {
    }

    static PaymentState toState(PaymentJpaEntity entity) {
        return new PaymentState(
            entity.getId(),
            entity.getOrderId(),
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

    static PaymentJpaEntity toEntity(PaymentState state) {
        return PaymentJpaEntity.create(
            state.orderId(),
            state.paymentMethod(),
            state.paymentStatus(),
            state.amount(),
            state.pgProvider(),
            state.pgTid(),
            state.pgOrderId(),
            state.cardCompany(),
            state.cardNumber(),
            state.installmentMonths(),
            state.approvedAt(),
            state.cancelledAt(),
            state.cancelReason(),
            state.receiptUrl()
        );
    }

    static void applyChanges(PaymentJpaEntity entity, PaymentState state) {
        entity.applyChanges(
            state.paymentStatus(),
            state.pgProvider(),
            state.pgTid(),
            state.pgOrderId(),
            state.cardCompany(),
            state.cardNumber(),
            state.installmentMonths(),
            state.approvedAt(),
            state.cancelledAt(),
            state.cancelReason(),
            state.receiptUrl()
        );
    }
}
