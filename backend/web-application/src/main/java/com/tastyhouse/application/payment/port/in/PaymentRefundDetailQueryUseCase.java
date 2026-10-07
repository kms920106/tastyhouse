package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentRefundViewResult;

public interface PaymentRefundDetailQueryUseCase {

    PaymentRefundViewResult getRefund(Long refundId);
}
