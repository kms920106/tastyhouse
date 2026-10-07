package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.payment.port.in.PaymentByOrderQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentResult;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class PaymentByOrderQueryService implements PaymentByOrderQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentByOrderQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    @Override
    public PaymentViewResult getPaymentByOrderId(Long memberId, Long orderId) {
        PaymentResult result = paymentQueryPort.findPaymentByOrderId(OrderId.of(orderId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        if (!memberId.equals(result.memberId())) {
            throw new DomainException(DomainErrorCode.ORDER_ACCESS_DENIED);
        }
        return PaymentViewResult.from(result);
    }
}
