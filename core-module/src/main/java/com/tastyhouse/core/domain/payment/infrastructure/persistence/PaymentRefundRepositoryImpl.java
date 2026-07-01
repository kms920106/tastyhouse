package com.tastyhouse.core.domain.payment.infrastructure.persistence;

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
        return paymentRefundJpaRepository.save(paymentRefund);
    }
}
