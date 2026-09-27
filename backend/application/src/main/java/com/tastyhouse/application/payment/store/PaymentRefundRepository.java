package com.tastyhouse.application.payment.store;

import com.tastyhouse.domain.payment.model.PaymentRefund;

public interface PaymentRefundRepository {
    PaymentRefund save(PaymentRefund paymentRefund);
}
