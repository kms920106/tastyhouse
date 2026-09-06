package com.tastyhouse.domain.payment.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentCancelCode;
import com.tastyhouse.domain.payment.model.PaymentRefund;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.repository.PaymentRefundRepository;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class PaymentCancellationService {
    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentCancellationService(
        PaymentRepository paymentRepository,
        PaymentRefundRepository paymentRefundRepository,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    public PaymentCancellationTarget prepareCancellation(MemberId memberId, PaymentId paymentId) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return PaymentCancellationTarget.rejected(cancelCode);
        }

        return PaymentCancellationTarget.cancellable(isPgCancelRequired(payment), payment.getPgTid());
    }

    public PaymentCancelCode applyCancellation(MemberId memberId, PaymentId paymentId, String cancelReason) {
        Payment payment = loadPayment(paymentId);
        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        PaymentCancelCode cancelCode = resolveCancelCode(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return cancelCode;
        }

        LocalDateTime now = LocalDateTime.now();
        payment.cancel(cancelReason, now);

        paymentRepository.save(payment);
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
        orderTransitionService.loadOwnedBy(payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED);

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (refundAmount > payment.getAmount().value()) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED);
        }

        Amount amount = new Amount(refundAmount);
        PaymentRefund savedRefund = paymentRefundRepository.save(
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
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
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
        return payment.getPgProvider() == PgProvider.TOSS
            && payment.getPaymentStatus() == PaymentStatus.COMPLETED;
    }
}
