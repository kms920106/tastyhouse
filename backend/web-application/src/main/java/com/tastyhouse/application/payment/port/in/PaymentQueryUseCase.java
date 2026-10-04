package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.payment.port.out.PaymentRefundViewResult;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;

public interface PaymentQueryUseCase {

    PaymentViewResult getPayment(Long memberId, Long id);

    PaymentViewResult getPayment(Long id);

    PaymentViewResult getPaymentByOrderId(Long memberId, Long orderId);

    PaymentRefundViewResult getRefund(Long refundId);
}
