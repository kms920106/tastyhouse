package com.tastyhouse.domain.payment.repository;

import com.tastyhouse.domain.payment.model.PaymentRefund;

public interface PaymentRefundRepository {
    PaymentRefund save(PaymentRefund paymentRefund);
}
