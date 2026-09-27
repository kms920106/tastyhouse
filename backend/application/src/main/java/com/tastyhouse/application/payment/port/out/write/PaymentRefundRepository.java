package com.tastyhouse.application.payment.port.out.write;

import com.tastyhouse.domain.payment.model.PaymentRefund;

public interface PaymentRefundRepository {
    PaymentRefund save(PaymentRefund paymentRefund);
}
