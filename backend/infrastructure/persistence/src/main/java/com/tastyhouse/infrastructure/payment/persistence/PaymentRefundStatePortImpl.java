package com.tastyhouse.infrastructure.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.payment.port.out.write.PaymentRefundState;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundStatePort;

@Repository
public class PaymentRefundStatePortImpl implements PaymentRefundStatePort {
    private final PaymentRefundJpaRepository paymentRefundJpaRepository;

    public PaymentRefundStatePortImpl(PaymentRefundJpaRepository paymentRefundJpaRepository) {
        this.paymentRefundJpaRepository = paymentRefundJpaRepository;
    }

    @Override
    public PaymentRefundState save(PaymentRefundState state) {
        PaymentRefundJpaEntity saved = paymentRefundJpaRepository.save(PaymentRefundMapper.toEntity(state));
        return PaymentRefundMapper.toState(saved);
    }
}
