package com.tastyhouse.infrastructure.payment.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRefundRepository;

@Repository
@RequiredArgsConstructor
public class PaymentRefundRepositoryImpl implements PaymentRefundRepository {

    private final PaymentRefundJpaRepository paymentRefundJpaRepository;

    @Override
    public PaymentRefund save(PaymentRefund paymentRefund) {
        PaymentRefundJpaEntity saved = paymentRefundJpaRepository.save(PaymentRefundMapper.toEntity(paymentRefund));
        return PaymentRefundMapper.toDomain(saved);
    }
}
