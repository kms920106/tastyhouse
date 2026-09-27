package com.tastyhouse.application.payment.port.out;

import java.util.Optional;

public interface PaymentQueryPort {

    Optional<PaymentResult> findPaymentByOrderId(Long orderId);

    Optional<PaymentResult> findPaymentById(Long paymentId);

    Optional<PaymentRefundResult> findRefundById(Long refundId);
}
