package com.tastyhouse.infrastructure.jpa.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundSavePort;

@Repository
class PaymentRefundPersistenceAdapter implements PaymentRefundSavePort {

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
