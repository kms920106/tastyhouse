package com.tastyhouse.application.payment.port.out.write;

public interface PaymentRefundStatePort {
    PaymentRefundState save(PaymentRefundState state);
}
