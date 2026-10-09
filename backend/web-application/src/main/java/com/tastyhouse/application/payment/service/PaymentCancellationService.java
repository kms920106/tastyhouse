package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentCancellationTarget;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentRefundSavePort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class PaymentCancellationService {

    private final PaymentLoadPort paymentLoadPort;
    private final PaymentSavePort paymentSavePort;
    private final PaymentRefundSavePort paymentRefundSavePort;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentCancellationService(
        PaymentLoadPort paymentLoadPort,
        PaymentSavePort paymentSavePort,
        PaymentRefundSavePort paymentRefundSavePort,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentLoadPort = paymentLoadPort;
        this.paymentSavePort = paymentSavePort;
        this.paymentRefundSavePort = paymentRefundSavePort;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    public PaymentCancellationTarget prepareCancellation(MemberId memberId, PaymentId paymentId) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return PaymentCancellationTarget.rejected(cancelCode);
        }

        return PaymentCancellationTarget.cancellable(
            isPgCancelRequired(payment),
            payment.getPgProvider(),
            payment.getPgTid()
        );
    }

    public PaymentCancelCode applyCancellation(MemberId memberId, PaymentId paymentId, String cancelReason) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return cancelCode;
        }

        LocalDateTime now = LocalDateTime.now();
        payment.cancel(cancelReason, now);

        paymentSavePort.save(payment);
        orderTransitionService.cancel(order);

        domainEventPublisher.publish(new PaymentCancelledEvent(
            paymentId,
            payment.getOrderId(),
            memberId,
            order.getUsedPoint(),
            order.getEarnedPoint(),
            cancelReason,
            now
        ));

        return PaymentCancelCode.SUCCESS;
    }

    public PaymentRefundId requestRefund(
        MemberId memberId,
        PaymentId paymentId,
        int refundAmount,
        String refundReason
    ) {
        Payment payment = loadPayment(paymentId);
        orderTransitionService.loadOwnedBy(payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED);

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new ApplicationException(WebErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (refundAmount > payment.getAmount().value()) {
            throw new ApplicationException(WebErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED);
        }

        Amount amount = new Amount(refundAmount);
        PaymentRefund savedRefund = paymentRefundSavePort.save(
            PaymentRefund.create(paymentId, amount, refundReason)
        );

        domainEventPublisher.publish(new RefundRequestedEvent(
            savedRefund.getPaymentRefundId(),
            paymentId,
            memberId,
            amount,
            refundReason,
            LocalDateTime.now()
        ));

        return savedRefund.getPaymentRefundId();
    }

    private Payment loadPayment(PaymentId paymentId) {
        return paymentLoadPort.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
    }

    private PaymentCancelCode resolveCancelCode(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PREPARING -> PaymentCancelCode.ALREADY_PREPARING;
            case CANCELLED -> PaymentCancelCode.ALREADY_CANCELLED;
            case COMPLETED -> PaymentCancelCode.ORDER_COMPLETED;
            case PENDING, CONFIRMED -> PaymentCancelCode.SUCCESS;
        };
    }

    private boolean isPgCancelRequired(Payment payment) {
        return payment.getPgProvider() != null
            && payment.getPaymentStatus() == PaymentStatus.COMPLETED;
    }
}
