package com.tastyhouse.application.payment.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.vo.PaymentId;

public interface PaymentLoadPort {

    Optional<Payment> findById(PaymentId paymentId);

    Optional<Payment> findByIdForUpdate(PaymentId paymentId);

    Optional<Payment> findByPgOrderId(String pgOrderId);

    Optional<Payment> findByPgOrderIdForUpdate(String pgOrderId);

    boolean existsByOrderId(OrderId orderId);

    List<Payment> findAllPgAttemptedReconcileDueByStatusCreatedBetween(
        PaymentStatus status, LocalDateTime from, LocalDateTime to, LocalDateTime dueAt, int limit
    );

    int findReconcileAttemptCount(PaymentId paymentId);
}
