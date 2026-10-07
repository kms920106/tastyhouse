package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.application.payment.port.in.PaymentRefundDetailQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentRefundViewResult;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PaymentRefundDetailQueryService implements PaymentRefundDetailQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentRefundDetailQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    @Override
    public PaymentRefundViewResult getRefund(Long refundId) {
        return paymentQueryPort.findRefundById(PaymentRefundId.of(refundId).value())
            .map(PaymentRefundViewResult::from)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.PAYMENT_REFUND_NOT_FOUND));
    }
}
