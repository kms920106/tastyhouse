package com.tastyhouse.infrastructure.payment.persistence;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.Amount;

final class PaymentMapper {
    private PaymentMapper() {
    }

    static Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
            entity.getId(),
            entity.getOrderId() == null ? null : OrderId.of(entity.getOrderId()),
            entity.getPaymentMethod() == null ? null : PaymentMethod.valueOf(entity.getPaymentMethod()),
            entity.getPaymentStatus() == null ? null : PaymentStatus.valueOf(entity.getPaymentStatus()),
            entity.getAmount() == null ? null : new Amount(entity.getAmount()),
            entity.getPgProvider() == null ? null : PgProvider.valueOf(entity.getPgProvider()),
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

    static PaymentJpaEntity toEntity(Payment payment) {
        return PaymentJpaEntity.create(
            payment.getOrderId() == null ? null : payment.getOrderId().value(),
            payment.getPaymentMethod() == null ? null : payment.getPaymentMethod().name(),
            payment.getPaymentStatus() == null ? null : payment.getPaymentStatus().name(),
            payment.getAmount() == null ? null : payment.getAmount().value(),
            payment.getPgProvider() == null ? null : payment.getPgProvider().name(),
            payment.getPgTid(),
            payment.getPgOrderId(),
            payment.getCardCompany(),
            payment.getCardNumber(),
            payment.getInstallmentMonths(),
            payment.getApprovedAt(),
            payment.getCancelledAt(),
            payment.getCancelReason(),
            payment.getReceiptUrl()
        );
    }

    static void applyChanges(PaymentJpaEntity entity, Payment payment) {
        entity.applyChanges(
            payment.getPaymentStatus() == null ? null : payment.getPaymentStatus().name(),
            payment.getPgProvider() == null ? null : payment.getPgProvider().name(),
            payment.getPgTid(),
            payment.getPgOrderId(),
            payment.getCardCompany(),
            payment.getCardNumber(),
            payment.getInstallmentMonths(),
            payment.getApprovedAt(),
            payment.getCancelledAt(),
            payment.getCancelReason(),
            payment.getReceiptUrl()
        );
    }
}
