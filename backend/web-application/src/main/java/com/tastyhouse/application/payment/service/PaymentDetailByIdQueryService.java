package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentDetailByIdQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class PaymentDetailByIdQueryService implements PaymentDetailByIdQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentDetailByIdQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    @Override
    public PaymentViewResult getPayment(Long id) {
        return paymentQueryPort.findPaymentById(PaymentId.of(id).value())
            .map(PaymentViewResult::from)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
    }
}
