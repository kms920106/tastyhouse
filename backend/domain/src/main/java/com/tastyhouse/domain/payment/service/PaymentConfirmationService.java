package com.tastyhouse.domain.payment.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.port.dto.PgConfirmResult;
import com.tastyhouse.domain.payment.port.dto.TossPaymentDetail;
import com.tastyhouse.domain.payment.repository.PaymentRepository;
import com.tastyhouse.domain.payment.repository.TossPaymentRecordRepository;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PgOrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class PaymentConfirmationService {
    private static final int CASH_POINT_EARN_RATE = 10;

    private final PaymentRepository paymentRepository;
    private final TossPaymentRecordRepository tossPaymentRecordRepository;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentConfirmationService(
        PaymentRepository paymentRepository,
        TossPaymentRecordRepository tossPaymentRecordRepository,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.tossPaymentRecordRepository = tossPaymentRecordRepository;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    public PaymentId open(MemberId memberId, OrderId orderId, PaymentMethod paymentMethod) {
        Order order = orderTransitionService.loadOwnedBy(orderId, memberId, ErrorCode.PAYMENT_ORDER_ACCESS_DENIED);

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_ORDER_STATUS);
        }

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS);
        }

        Payment payment = Payment.create(
            orderId,
            paymentMethod,
            new Amount(order.getFinalAmount()),
            PgOrderId.generate()
        );
        return paymentRepository.save(payment).getPaymentId();
    }

    public PaymentId confirm(PaymentId paymentId, PgConfirmation confirmation) {
        Payment payment = loadPendingPayment(paymentId);
        Order order = orderTransitionService.load(payment.getOrderId());

        payment.updatePgInfo(confirmation.pgProvider(), confirmation.pgTid(), confirmation.pgOrderId());

        if (confirmation.cardCompany() != null) {
            payment.updateCardInfo(
                confirmation.cardCompany(),
                confirmation.cardNumber(),
                confirmation.installmentMonths()
            );
        }

        payment.complete(confirmation.pgTid(), LocalDateTime.now(), confirmation.receiptUrl());

        Payment savedPayment = paymentRepository.save(payment);
        orderTransitionService.confirm(order);

        return savedPayment.getPaymentId();
    }

    public TossConfirmationTarget prepareTossConfirmation(MemberId memberId, String pgOrderId, int amount) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        orderTransitionService.loadOwnedBy(payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (!payment.getAmount().value().equals(amount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return new TossConfirmationTarget(payment.getId(), pgOrderId, amount);
    }

    public PaymentId applyTossConfirmation(MemberId memberId, String pgOrderId, PgConfirmResult result) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        tossPaymentRecordRepository.save(toTossPaymentRecord(payment.getPaymentId(), result.detail()));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        payment.updatePgInfo(PgProvider.TOSS, result.paymentKey(), pgOrderId);

        if (result.cardCompany() != null) {
            payment.updateCardInfo(result.cardCompany(), result.cardNumber(), result.installmentPlanMonths());
        }

        payment.complete(result.paymentKey(), result.approvedAt(), result.receiptUrl());

        Payment savedPayment = paymentRepository.save(payment);
        orderTransitionService.confirm(order);

        domainEventPublisher.publish(new PaymentCompletedEvent(
            savedPayment.getPaymentId(),
            savedPayment.getOrderId(),
            memberId,
            savedPayment.getAmount(),
            savedPayment.getPaymentMethod(),
            false,
            savedPayment.getApprovedAt()
        ));

        return savedPayment.getPaymentId();
    }

    public void failTossConfirmation(String pgOrderId, PgConfirmResult result) {
        Payment payment = paymentRepository.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        tossPaymentRecordRepository.save(toTossPaymentRecord(payment.getPaymentId(), result.detail()));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.fail();
        paymentRepository.save(payment);
    }

    public PaymentId completeOnSitePayment(MemberId memberId, PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ErrorCode.PAYMENT_ACCESS_DENIED
        );

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING);
        }

        if (!isOnSitePayment(payment.getPaymentMethod())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ON_SITE);
        }

        LocalDateTime now = LocalDateTime.now();
        payment.complete(null, now, null);
        order.updateEarnedPoint(calculateEarnedPoint(payment.getAmount()));

        Payment savedPayment = paymentRepository.save(payment);
        orderTransitionService.confirm(order);

        domainEventPublisher.publish(new PaymentCompletedEvent(
            savedPayment.getPaymentId(),
            savedPayment.getOrderId(),
            memberId,
            savedPayment.getAmount(),
            savedPayment.getPaymentMethod(),
            true,
            now
        ));

        return savedPayment.getPaymentId();
    }

    public int calculateEarnedPoint(Amount amount) {
        return (int) (amount.value() * CASH_POINT_EARN_RATE / 100.0);
    }

    public int earnRate() {
        return CASH_POINT_EARN_RATE;
    }

    private Payment loadPendingPayment(PaymentId paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }
        return payment;
    }

    private boolean isOnSitePayment(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CASH_ON_SITE || paymentMethod == PaymentMethod.CARD_ON_SITE;
    }

    private TossPaymentRecord toTossPaymentRecord(PaymentId paymentId, TossPaymentDetail detail) {
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
}
