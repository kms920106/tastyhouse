package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.application.payment.port.in.PaymentDetailQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentResult;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class PaymentDetailQueryService implements PaymentDetailQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentDetailQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    @Override
    public PaymentViewResult getPayment(Long memberId, Long id) {
        PaymentResult result = paymentQueryPort.findPaymentById(PaymentId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        if (!memberId.equals(result.memberId())) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_ACCESS_DENIED);
        }
        return PaymentViewResult.from(result);
    }
}
