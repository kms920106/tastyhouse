package com.tastyhouse.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.application.payment.port.in.PaymentQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentRefundResult;
import com.tastyhouse.application.payment.port.out.PaymentRefundViewResult;
import com.tastyhouse.application.payment.port.out.PaymentResult;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class PaymentQueryService implements PaymentQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    @Override
    public PaymentViewResult getPayment(Long memberId, Long id) {
        return toPaymentViewResult(
            validatePaymentOwnership(loadPayment(id), memberId)
        );
    }

    @Override
    public PaymentViewResult getPayment(Long id) {
        return toPaymentViewResult(loadPayment(id));
    }

    private PaymentResult loadPayment(Long id) {
        return paymentQueryPort.findPaymentById(PaymentId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public PaymentViewResult getPaymentByOrderId(Long memberId, Long orderId) {
        PaymentResult result = paymentQueryPort.findPaymentByOrderId(OrderId.of(orderId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        return toPaymentViewResult(validateOrderOwnership(result, memberId));
    }

    @Override
    public PaymentRefundViewResult getRefund(Long refundId) {
        PaymentRefundResult result = paymentQueryPort.findRefundById(PaymentRefundId.of(refundId).value())
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.PAYMENT_REFUND_NOT_FOUND));
        return toPaymentRefundViewResult(result);
    }

    private PaymentResult validateOrderOwnership(PaymentResult result, Long memberId) {
        if (!memberId.equals(result.memberId())) {
            throw new DomainException(DomainErrorCode.ORDER_ACCESS_DENIED);
        }
        return result;
    }

    private PaymentResult validatePaymentOwnership(PaymentResult result, Long memberId) {
        if (!memberId.equals(result.memberId())) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_ACCESS_DENIED);
        }
        return result;
    }

    private PaymentViewResult toPaymentViewResult(PaymentResult result) {
        return new PaymentViewResult(
            result.id(),
            result.orderId(),
            result.paymentMethod(),
            result.paymentStatus(),
            result.amount(),
            result.pgProvider(),
            result.pgTid(),
            result.pgOrderId(),
            result.cardCompany(),
            result.cardNumber(),
            result.installmentMonths(),
            result.approvedAt(),
            result.cancelledAt(),
            result.cancelReason(),
            result.receiptUrl(),
            result.createdAt()
        );
    }

    private PaymentRefundViewResult toPaymentRefundViewResult(PaymentRefundResult result) {
        return new PaymentRefundViewResult(
            result.id(),
            result.paymentId(),
            result.refundAmount(),
            result.refundReason(),
            result.refundStatus(),
            result.pgRefundId(),
            result.refundedAt(),
            result.createdAt()
        );
    }
}
