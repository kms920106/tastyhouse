package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentViewResult;

public interface PaymentByOrderQueryUseCase {

    PaymentViewResult getPaymentByOrderId(Long memberId, Long orderId);
}
