package com.tastyhouse.application.payment.port.out.write;

import java.util.Optional;

public interface PaymentStatePort {
    Optional<PaymentState> findById(Long paymentId);

    Optional<PaymentState> findByPgOrderId(String pgOrderId);

    boolean existsByOrderId(Long orderId);

    PaymentState save(PaymentState state);
}
