package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.model.Payment;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.model.TossPaymentRecord;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PgOrderId;
import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.TossPaymentDetail;
import com.tastyhouse.application.payment.port.out.write.PaymentLoadPort;
import com.tastyhouse.application.payment.port.out.write.PaymentSavePort;
import com.tastyhouse.application.payment.port.out.write.TossPaymentRecordSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class PaymentConfirmationService {

    private static final int CASH_POINT_EARN_RATE = 10;

    private final PaymentLoadPort paymentLoadPort;
    private final PaymentSavePort paymentSavePort;
    private final TossPaymentRecordSavePort tossPaymentRecordSavePort;
    private final OrderTransitionService orderTransitionService;
    private final DomainEventPublisher domainEventPublisher;

    public PaymentConfirmationService(
        PaymentLoadPort paymentLoadPort,
        PaymentSavePort paymentSavePort,
        TossPaymentRecordSavePort tossPaymentRecordSavePort,
        OrderTransitionService orderTransitionService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.paymentLoadPort = paymentLoadPort;
        this.paymentSavePort = paymentSavePort;
        this.tossPaymentRecordSavePort = tossPaymentRecordSavePort;
        this.orderTransitionService = orderTransitionService;
        this.domainEventPublisher = domainEventPublisher;
    }

    public PaymentId open(MemberId memberId, OrderId orderId, PaymentMethod paymentMethod) {
        Order order = orderTransitionService.loadOwnedBy(orderId, memberId, ApplicationErrorCode.PAYMENT_ORDER_ACCESS_DENIED);

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_INVALID_ORDER_STATUS);
        }

        if (paymentLoadPort.existsByOrderId(orderId)) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_ALREADY_IN_PROGRESS);
        }

        Payment payment = Payment.create(
            orderId,
            paymentMethod,
            new Amount(order.getFinalAmount()),
            PgOrderId.generate()
        );
        return paymentSavePort.save(payment).getPaymentId();
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

        Payment savedPayment = paymentSavePort.save(payment);
        orderTransitionService.confirm(order);

        return savedPayment.getPaymentId();
    }

    public PgConfirmationTarget preparePgConfirmation(MemberId memberId, String pgOrderId, int amount) {
        Payment payment = paymentLoadPort.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));

        orderTransitionService.loadOwnedBy(payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (!payment.getAmount().value().equals(amount)) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return new PgConfirmationTarget(payment.getId(), pgOrderId, amount);
    }

    public PaymentId applyPgConfirmation(
        MemberId memberId,
        PgProvider pgProvider,
        String pgOrderId,
        PgConfirmResult result
    ) {
        Payment payment = paymentLoadPort.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED
        );

        recordTossDetail(payment.getPaymentId(), result.detail());

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        payment.updatePgInfo(pgProvider, result.paymentKey(), pgOrderId);

        if (result.cardCompany() != null) {
            payment.updateCardInfo(result.cardCompany(), result.cardNumber(), result.installmentPlanMonths());
        }

        payment.complete(result.paymentKey(), result.approvedAt(), result.receiptUrl());

        Payment savedPayment = paymentSavePort.save(payment);
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

    public void failPgConfirmation(String pgOrderId, PgConfirmResult result) {
        Payment payment = paymentLoadPort.findByPgOrderId(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));

        recordTossDetail(payment.getPaymentId(), result.detail());

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.fail();
        paymentSavePort.save(payment);
    }

    public PaymentId completeOnSitePayment(MemberId memberId, PaymentId paymentId) {
        Payment payment = paymentLoadPort.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED
        );

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new DomainException(DomainErrorCode.PAYMENT_NOT_PENDING);
        }

        if (!isOnSitePayment(payment.getPaymentMethod())) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_ON_SITE);
        }

        LocalDateTime now = LocalDateTime.now();
        payment.complete(null, now, null);
        order.updateEarnedPoint(calculateEarnedPoint(payment.getAmount()));

        Payment savedPayment = paymentSavePort.save(payment);
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
        Payment payment = paymentLoadPort.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }
        return payment;
    }

    private boolean isOnSitePayment(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CASH_ON_SITE || paymentMethod == PaymentMethod.CARD_ON_SITE;
    }

    private void recordTossDetail(PaymentId paymentId, TossPaymentDetail detail) {
        if (detail == null) {
            return;
        }
        tossPaymentRecordSavePort.save(toTossPaymentRecord(paymentId, detail));
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
