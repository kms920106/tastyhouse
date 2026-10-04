package com.tastyhouse.infrastructure.persistence.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundPersistencePort;

@Repository
public class PaymentRefundPersistenceAdapter implements PaymentRefundPersistencePort {

    private final PaymentRefundJpaRepository paymentRefundJpaRepository;

    public PaymentRefundPersistenceAdapter(PaymentRefundJpaRepository paymentRefundJpaRepository) {
        this.paymentRefundJpaRepository = paymentRefundJpaRepository;
    }

    @Override
    public PaymentRefund save(PaymentRefund paymentRefund) {
        PaymentRefundJpaEntity saved = paymentRefundJpaRepository.save(PaymentRefundMapper.toEntity(paymentRefund));
        return PaymentRefundMapper.toDomain(saved);
    }
}
