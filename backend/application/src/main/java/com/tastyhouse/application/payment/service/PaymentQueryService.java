package com.tastyhouse.application.payment.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.application.payment.port.out.PaymentQueryPort;
import com.tastyhouse.application.payment.port.out.PaymentRefundResult;
import com.tastyhouse.application.payment.port.out.PaymentResult;
import com.tastyhouse.application.payment.port.in.PaymentQueryUseCase;
import com.tastyhouse.application.payment.port.out.PaymentRefundViewResult;
import com.tastyhouse.application.payment.port.out.PaymentViewResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class PaymentQueryService implements PaymentQueryUseCase {

    private final PaymentQueryPort paymentQueryPort;

    public PaymentQueryService(PaymentQueryPort paymentQueryPort) {
        this.paymentQueryPort = paymentQueryPort;
    }

    @Override
    public PaymentViewResult getPayment(Long memberId, Long id) {
        return toPaymentViewResult(
            validateOwnership(loadPayment(id), memberId, ErrorCode.PAYMENT_ACCESS_DENIED)
        );
    }

    @Override
    public PaymentViewResult getPayment(Long id) {
        return toPaymentViewResult(loadPayment(id));
    }

    private PaymentResult loadPayment(Long id) {
        return paymentQueryPort.findPaymentById(PaymentId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public PaymentViewResult getPaymentByOrderId(Long memberId, Long orderId) {
        PaymentResult result = paymentQueryPort.findPaymentByOrderId(OrderId.of(orderId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
        return toPaymentViewResult(validateOwnership(result, memberId, ErrorCode.ORDER_ACCESS_DENIED));
    }

    @Override
    public PaymentRefundViewResult getRefund(Long refundId) {
        PaymentRefundResult result = paymentQueryPort.findRefundById(PaymentRefundId.of(refundId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_REFUND_NOT_FOUND));
        return toPaymentRefundViewResult(result);
    }

    private PaymentResult validateOwnership(PaymentResult result, Long memberId, ErrorCode accessDeniedCode) {
        if (!memberId.equals(result.memberId())) {
            throw new BusinessException(accessDeniedCode);
        }
        return result;
    }

    private PaymentViewResult toPaymentViewResult(PaymentResult result) {
        return new PaymentViewResult(
            result.id(),
            result.orderId(),
            result.paymentMethod() == null ? null : result.paymentMethod().name(),
            result.paymentStatus() == null ? null : result.paymentStatus().name(),
            result.amount() == null ? null : result.amount().value(),
            result.pgProvider() == null ? null : result.pgProvider().name(),
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
            result.refundAmount() == null ? null : result.refundAmount().value(),
            result.refundReason(),
            result.refundStatus() == null ? null : result.refundStatus().name(),
            result.pgRefundId(),
            result.refundedAt(),
            result.createdAt()
        );
    }
}
