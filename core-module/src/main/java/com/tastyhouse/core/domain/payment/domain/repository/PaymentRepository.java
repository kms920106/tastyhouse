package com.tastyhouse.core.domain.payment.domain.repository;

import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByOrderId(OrderId orderId);

    Optional<Payment> findByPgTid(String pgTid);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);

    boolean existsByOrderId(OrderId orderId);

    Payment save(Payment payment);
}
