package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;

public interface PaymentRefundRepository {

    PaymentRefund save(PaymentRefund paymentRefund);
}
