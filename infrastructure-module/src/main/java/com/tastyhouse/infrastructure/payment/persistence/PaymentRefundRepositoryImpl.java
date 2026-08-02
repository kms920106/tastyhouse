package com.tastyhouse.infrastructure.payment.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.domain.payment.domain.repository.PaymentRefundRepository;

@Repository
public class PaymentRefundRepositoryImpl implements PaymentRefundRepository {

    private final PaymentRefundJpaRepository paymentRefundJpaRepository;

    public PaymentRefundRepositoryImpl(PaymentRefundJpaRepository paymentRefundJpaRepository) {
        this.paymentRefundJpaRepository = paymentRefundJpaRepository;
    }

    @Override
    public PaymentRefund save(PaymentRefund paymentRefund) {
        PaymentRefundJpaEntity saved = paymentRefundJpaRepository.save(PaymentRefundMapper.toEntity(paymentRefund));
        return PaymentRefundMapper.toDomain(saved);
    }
}
