package com.tastyhouse.application.payment.store;

import java.util.Optional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.out.write.PaymentStatePort;

public class PaymentStore implements PaymentRepository {
    private final PaymentStatePort paymentStatePort;

    public PaymentStore(PaymentStatePort paymentStatePort) {
        this.paymentStatePort = paymentStatePort;
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return paymentStatePort.findById(paymentId.value()).map(PaymentStateMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return paymentStatePort.findByPgOrderId(pgOrderId).map(PaymentStateMapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return paymentStatePort.existsByOrderId(orderId.value());
    }

    @Override
    public Payment save(Payment payment) {
        return PaymentStateMapper.toDomain(paymentStatePort.save(PaymentStateMapper.toState(payment)));
    }
}
