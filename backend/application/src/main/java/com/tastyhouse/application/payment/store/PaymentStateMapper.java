package com.tastyhouse.application.payment.store;

import com.tastyhouse.application.payment.port.out.write.PaymentState;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.Amount;

final class PaymentStateMapper {
    private PaymentStateMapper() {
    }

    static Payment toDomain(PaymentState state) {
        return Payment.reconstitute(
            state.id(),
            state.orderId() == null ? null : OrderId.of(state.orderId()),
            state.paymentMethod() == null ? null : PaymentMethod.valueOf(state.paymentMethod()),
            state.paymentStatus() == null ? null : PaymentStatus.valueOf(state.paymentStatus()),
            state.amount() == null ? null : new Amount(state.amount()),
            state.pgProvider() == null ? null : PgProvider.valueOf(state.pgProvider()),
            state.pgTid(),
            state.pgOrderId(),
            state.cardCompany(),
            state.cardNumber(),
            state.installmentMonths(),
            state.approvedAt(),
            state.cancelledAt(),
            state.cancelReason(),
            state.receiptUrl(),
            state.createdAt()
        );
    }

    static PaymentState toState(Payment payment) {
        return new PaymentState(
            payment.getId(),
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
            payment.getReceiptUrl(),
            payment.getCreatedAt()
        );
    }
}
