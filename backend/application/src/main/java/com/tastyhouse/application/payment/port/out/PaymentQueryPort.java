package com.tastyhouse.application.payment.port.out;

import java.util.Optional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;

public interface PaymentQueryPort {

    Optional<PaymentResult> findPaymentByOrderId(OrderId orderId);

    Optional<PaymentResult> findPaymentById(PaymentId paymentId);

    Optional<PaymentRefundResult> findRefundById(PaymentRefundId refundId);
}
