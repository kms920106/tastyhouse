package com.tastyhouse.core.domain.payment.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

public interface PaymentRepository {

    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByOrderId(OrderId orderId);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    boolean existsByOrderId(OrderId orderId);

    Payment save(Payment payment);
}
