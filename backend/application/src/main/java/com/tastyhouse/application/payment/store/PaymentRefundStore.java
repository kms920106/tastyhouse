package com.tastyhouse.application.payment.store;

import com.tastyhouse.application.payment.port.out.write.PaymentRefundStatePort;
import com.tastyhouse.domain.payment.model.PaymentRefund;

public class PaymentRefundStore implements PaymentRefundRepository {
    private final PaymentRefundStatePort paymentRefundStatePort;

    public PaymentRefundStore(PaymentRefundStatePort paymentRefundStatePort) {
        this.paymentRefundStatePort = paymentRefundStatePort;
    }

    @Override
    public PaymentRefund save(PaymentRefund paymentRefund) {
        return PaymentRefundStateMapper.toDomain(paymentRefundStatePort.save(PaymentRefundStateMapper.toState(paymentRefund)));
    }
}
