package com.tastyhouse.application.payment.port.out.write;

import com.tastyhouse.domain.payment.model.PaymentRefund;

public interface PaymentRefundSavePort {

    PaymentRefund save(PaymentRefund paymentRefund);
}
