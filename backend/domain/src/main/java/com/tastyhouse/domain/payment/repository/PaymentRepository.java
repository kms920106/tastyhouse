package com.tastyhouse.domain.payment.repository;

import java.util.Optional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.vo.PaymentId;

public interface PaymentRepository {
    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    boolean existsByOrderId(OrderId orderId);

    Payment save(Payment payment);
}
