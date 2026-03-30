package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.payment.Payment;
import com.tastyhouse.core.entity.payment.PaymentRefund;
import com.tastyhouse.core.entity.payment.TossPaymentRecord;
import com.tastyhouse.core.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCoreService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByPgOrderId(String pgOrderId) {
        return paymentRepository.findByPgOrderId(pgOrderId);
    }

    @Transactional(readOnly = true)
    public boolean existsByOrderId(Long orderId) {
        return paymentRepository.existsByOrderId(orderId);
    }

    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentRefund saveRefund(PaymentRefund paymentRefund) {
        return paymentRepository.saveRefund(paymentRefund);
    }

    @Transactional
    public TossPaymentRecord saveTossRecord(TossPaymentRecord tossPaymentRecord) {
        return paymentRepository.saveTossRecord(tossPaymentRecord);
    }
}
