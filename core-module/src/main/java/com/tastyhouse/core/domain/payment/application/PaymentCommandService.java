package com.tastyhouse.core.domain.payment.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.event.PaymentCancelledEvent;
import com.tastyhouse.core.domain.payment.domain.event.PaymentCompletedEvent;
import com.tastyhouse.core.domain.payment.domain.event.RefundRequestedEvent;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentCancelCode;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.model.PgProvider;
import com.tastyhouse.core.domain.payment.domain.model.TossPaymentRecord;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRefundRepository;
import com.tastyhouse.core.domain.payment.domain.repository.PaymentRepository;
import com.tastyhouse.core.domain.payment.domain.repository.TossPaymentRecordRepository;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.core.domain.payment.domain.vo.PgOrderId;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.payment.application.dto.command.CancelPaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.ConfirmPaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.PaymentCreateCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.RequestRefundCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.TossConfirmCommand;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentCancelResult;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentRefundResult;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentResult;
import com.tastyhouse.core.domain.payment.application.port.PgPaymentGateway;
import com.tastyhouse.core.domain.payment.application.port.dto.PgCancelResult;
import com.tastyhouse.core.domain.payment.application.port.dto.PgConfirmResult;
import com.tastyhouse.core.domain.payment.application.port.dto.TossPaymentDetail;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final TossPaymentRecordRepository tossPaymentRecordRepository;
    private final PgPaymentGateway pgPaymentGateway;
    private final OrderQueryService orderQueryService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int CASH_POINT_EARN_RATE = 10;

    @Transactional
    public PaymentResult createPayment(MemberId memberId, PaymentCreateCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Order order = orderQueryService.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ORDER_ACCESS_DENIED);
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_ORDER_STATUS);
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS);
        }

        Payment payment = Payment.create(
            orderId,
            command.paymentMethod(),
            new Amount(order.getFinalAmount()),
            PgOrderId.generate()
        );

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResult.from(savedPayment);
    }

    @Transactional
    public PaymentResult confirmPayment(ConfirmPaymentCommand command) {
        PaymentId paymentId = PaymentId.of(command.paymentId());
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        Order order = orderQueryService.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        payment.updatePgInfo(command.pgProvider(), command.pgTid(), command.pgOrderId());

        if (command.cardCompany() != null) {
            payment.updateCardInfo(command.cardCompany(), command.cardNumber(), command.installmentMonths());
        }

        payment.complete(command.pgTid(), LocalDateTime.now(), command.receiptUrl());
        order.confirm();

        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult confirmTossPayment(MemberId memberId, TossConfirmCommand command) {
        Payment payment = paymentRepository.findByPgOrderId(command.pgOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderQueryService.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (!payment.getAmount().value().equals(command.amount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PgConfirmResult result = pgPaymentGateway.confirmPayment(
            payment.getId(), command.paymentKey(), command.pgOrderId(), command.amount()
        );

        tossPaymentRecordRepository.save(buildTossPaymentRecord(payment.getId(), result.detail()));

        if (!result.success()) {
            log.error("PG payment confirm failed. pgOrderId: {}, errorCode: {}, errorMessage: {}",
                command.pgOrderId(), result.errorCode(), result.errorMessage());
            payment.fail();
            throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED,
                result.errorMessage() != null ? result.errorMessage() : ErrorCode.PAYMENT_APPROVAL_FAILED.getDefaultMessage());
        }

        payment.updatePgInfo(PgProvider.TOSS, result.paymentKey(), command.pgOrderId());

        if (result.cardCompany() != null) {
            payment.updateCardInfo(result.cardCompany(), result.cardNumber(), result.installmentPlanMonths());
        }

        payment.complete(result.paymentKey(), result.approvedAt(), result.receiptUrl());
        order.confirm();

        log.info("Toss payment confirmed. paymentId: {}, orderId: {}, amount: {}",
            payment.getId(), payment.getOrderId().value(), command.amount());

        eventPublisher.publishEvent(new PaymentCompletedEvent(
            payment.getPaymentId(),
            payment.getOrderId(),
            memberId,
            payment.getAmount(),
            payment.getPaymentMethod(),
            false,
            payment.getApprovedAt()
        ));

        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentCancelResult cancelPayment(MemberId memberId, Long paymentIdValue, CancelPaymentCommand command) {
        PaymentId paymentId = PaymentId.of(paymentIdValue);
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderQueryService.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        PaymentCancelCode cancelCode = validateOrderStatusForCancel(order.getOrderStatus());
        if (cancelCode != PaymentCancelCode.SUCCESS) {
            return PaymentCancelResult.of(cancelCode);
        }

        if (payment.getPgProvider() == PgProvider.TOSS
            && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            try {
                PgCancelResult cancelResult = pgPaymentGateway.cancelPayment(
                    payment.getPgTid(), command.cancelReason()
                );
                if (!cancelResult.success()) {
                    log.error("PG payment cancel failed. paymentId: {}, errorCode: {}, errorMessage: {}",
                        paymentIdValue, cancelResult.errorCode(), cancelResult.errorMessage());
                    return PaymentCancelResult.of(PaymentCancelCode.CANCEL_FAILED);
                }
            } catch (Exception e) {
                log.error("PG payment cancel exception. paymentId: {}", paymentIdValue, e);
                return PaymentCancelResult.of(PaymentCancelCode.CANCEL_FAILED);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        payment.cancel(command.cancelReason(), now);
        order.cancel();

        eventPublisher.publishEvent(new PaymentCancelledEvent(
            paymentId,
            payment.getOrderId(),
            memberId,
            order.getUsedPoint(),
            order.getEarnedPoint(),
            command.cancelReason(),
            now
        ));

        return PaymentCancelResult.of(PaymentCancelCode.SUCCESS);
    }

    @Transactional
    public PaymentRefundResult requestRefund(MemberId memberId, Long paymentIdValue, RequestRefundCommand command) {
        PaymentId paymentId = PaymentId.of(paymentIdValue);
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderQueryService.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_COMPLETED);
        }

        if (command.refundAmount() > payment.getAmount().value()) {
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_AMOUNT_EXCEEDED);
        }

        Amount refundAmount = new Amount(command.refundAmount());
        PaymentRefund refund = PaymentRefund.create(paymentId, refundAmount, command.refundReason());
        PaymentRefund savedRefund = paymentRefundRepository.save(refund);

        eventPublisher.publishEvent(new RefundRequestedEvent(
            savedRefund.getPaymentRefundId(),
            paymentId,
            memberId,
            refundAmount,
            command.refundReason(),
            LocalDateTime.now()
        ));

        return PaymentRefundResult.from(savedRefund);
    }

    @Transactional
    public PaymentResult completeOnSitePayment(MemberId memberId, Long paymentIdValue) {
        PaymentId paymentId = PaymentId.of(paymentIdValue);
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "결제를 찾을 수 없습니다."));

        Order order = orderQueryService.findById(payment.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
        }

        if (!isOnSitePayment(payment.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ON_SITE);
        }

        LocalDateTime now = LocalDateTime.now();
        payment.complete(null, now, null);
        order.confirm();

        int earnedPoint = (int) (payment.getAmount().value() * CASH_POINT_EARN_RATE / 100.0);
        order.updateEarnedPoint(earnedPoint);

        eventPublisher.publishEvent(new PaymentCompletedEvent(
            paymentId,
            payment.getOrderId(),
            memberId,
            payment.getAmount(),
            payment.getPaymentMethod(),
            true,
            now
        ));

        return PaymentResult.from(payment);
    }

    private TossPaymentRecord buildTossPaymentRecord(Long paymentId, TossPaymentDetail detail) {
        return TossPaymentRecord.create(
            paymentId,
            detail.version(),
            detail.paymentKey(),
            detail.type(),
            detail.orderId(),
            detail.orderName(),
            detail.mId(),
            detail.currency(),
            detail.method(),
            detail.totalAmount(),
            detail.balanceAmount(),
            detail.status(),
            detail.requestedAt(),
            detail.approvedAt(),
            detail.useEscrow(),
            detail.lastTransactionKey(),
            detail.suppliedAmount(),
            detail.vat(),
            detail.cultureExpense(),
            detail.taxFreeAmount(),
            detail.taxExemptionAmount(),
            detail.partialCancelable(),
            detail.cardAmount(),
            detail.cardIssuerCode(),
            detail.cardAcquirerCode(),
            detail.cardNumber(),
            detail.cardInstallmentPlanMonths(),
            detail.cardApproveNo(),
            detail.cardUseCardPoint(),
            detail.cardType(),
            detail.cardOwnerType(),
            detail.cardAcquireStatus(),
            detail.cardInterestFree(),
            detail.cardInterestPayer(),
            detail.virtualAccountType(),
            detail.virtualAccountNumber(),
            detail.virtualAccountBankCode(),
            detail.virtualAccountCustomerName(),
            detail.virtualAccountDueDate(),
            detail.virtualAccountRefundStatus(),
            detail.virtualAccountExpired(),
            detail.virtualAccountSettlementStatus(),
            detail.mobilePhoneCustomerMobilePhone(),
            detail.mobilePhoneSettlementStatus(),
            detail.mobilePhoneReceiptUrl(),
            detail.transferBankCode(),
            detail.transferSettlementStatus(),
            detail.easyPayProvider(),
            detail.easyPayAmount(),
            detail.easyPayDiscountAmount(),
            detail.receiptUrl(),
            detail.checkoutUrl(),
            detail.failureCode(),
            detail.failureMessage(),
            detail.country()
        );
    }

    private boolean isOnSitePayment(PaymentMethod method) {
        return method == PaymentMethod.CASH_ON_SITE || method == PaymentMethod.CARD_ON_SITE;
    }

    private PaymentCancelCode validateOrderStatusForCancel(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PREPARING -> PaymentCancelCode.ALREADY_PREPARING;
            case CANCELLED -> PaymentCancelCode.ALREADY_CANCELLED;
            case COMPLETED -> PaymentCancelCode.ORDER_COMPLETED;
            case PENDING, CONFIRMED -> PaymentCancelCode.SUCCESS;
        };
    }
}
