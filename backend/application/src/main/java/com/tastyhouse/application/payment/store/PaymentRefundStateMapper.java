package com.tastyhouse.application.payment.store;

import com.tastyhouse.application.payment.port.out.write.PaymentRefundState;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;

final class PaymentRefundStateMapper {
    private PaymentRefundStateMapper() {
    }

    static PaymentRefund toDomain(PaymentRefundState state) {
        return PaymentRefund.reconstitute(
            state.id(),
            state.paymentId() == null ? null : PaymentId.of(state.paymentId()),
            state.refundAmount() == null ? null : new Amount(state.refundAmount()),
            state.refundReason(),
            state.refundStatus() == null ? null : RefundStatus.valueOf(state.refundStatus()),
            state.pgRefundId(),
            state.refundedAt(),
            state.createdAt()
        );
    }

    static PaymentRefundState toState(PaymentRefund paymentRefund) {
        return new PaymentRefundState(
            paymentRefund.getId(),
            paymentRefund.getPaymentId() == null ? null : paymentRefund.getPaymentId().value(),
            paymentRefund.getRefundAmount() == null ? null : paymentRefund.getRefundAmount().value(),
            paymentRefund.getRefundReason(),
            paymentRefund.getRefundStatus() == null ? null : paymentRefund.getRefundStatus().name(),
            paymentRefund.getPgRefundId(),
            paymentRefund.getRefundedAt(),
            paymentRefund.getCreatedAt()
        );
    }
}
