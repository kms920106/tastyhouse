package com.tastyhouse.application.payment.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentConfirmationService.class);

    private static final int CASH_POINT_EARN_RATE = 10;

    private static final int RECONCILE_SETTLE_MINUTES = 10;

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

    public void recordPgAttempt(String pgOrderId, PgProvider pgProvider) {
        Payment payment = loadForUpdate(pgOrderId);
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }
        payment.recordPgAttempt(pgProvider);
        paymentSavePort.save(payment);
        paymentSavePort.saveReconcileSchedule(
            payment.getPaymentId(), 0, LocalDateTime.now().plusMinutes(RECONCILE_SETTLE_MINUTES)
        );
    }

    public PaymentId applyPgConfirmation(
        MemberId memberId,
        PgProvider pgProvider,
        String pgOrderId,
        PgConfirmResult result
    ) {
        Payment payment = loadForUpdate(pgOrderId);

        Order order = orderTransitionService.loadOwnedBy(
            payment.getOrderId(), memberId, ApplicationErrorCode.PAYMENT_ACCESS_DENIED
        );

        if (isAlreadyAppliedApproval(payment, result)) {
            return payment.getPaymentId();
        }

        recordTossDetail(payment.getPaymentId(), result.detail());

        if (isNotAwaitingPgApproval(payment.getPaymentStatus())) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        Payment savedPayment = markApproved(payment, pgProvider, pgOrderId, result);
        confirmOrderAndPublish(savedPayment, order, memberId);
        return savedPayment.getPaymentId();
    }

    public PgReconcileResult reconcileApproved(String pgOrderId, PgProvider pgProvider, PgConfirmResult approval) {
        Payment payment = loadForUpdate(pgOrderId);

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            return new PgReconcileResult(payment.getPaymentId(), PgReconcileOutcome.ALREADY_COMPLETED);
        }

        if (isNotAwaitingPgApproval(payment.getPaymentStatus())) {
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_PENDING_APPROVAL);
        }

        if (approval.totalAmount() == null || !approval.totalAmount().equals(payment.getAmount().value())) {
            log.error(
                "{} PG 조회로 승인을 확인했으나 금액이 결제와 다름 — pgOrderId={}, paymentId={}, paymentAmount={}, pgAmount={}, paymentKey={}",
                PaymentLogMarkers.PG_DB_MISMATCH, pgOrderId, payment.getId(), payment.getAmount().value(),
                approval.totalAmount(), approval.paymentKey()
            );
            return new PgReconcileResult(payment.getPaymentId(), PgReconcileOutcome.AMOUNT_MISMATCH);
        }

        recordTossDetail(payment.getPaymentId(), approval.detail());

        Order order = orderTransitionService.load(payment.getOrderId());
        Payment savedPayment = markApproved(payment, pgProvider, pgOrderId, approval);

        if (!order.getOrderStatus().canTransitionTo(OrderStatus.CONFIRMED)) {
            log.error(
                "{} PG 조회로 승인을 확인했으나 주문을 확정할 수 없어 결제만 완료로 반영(환불 필요) — pgOrderId={}, paymentId={}, orderStatus={}, paymentKey={}",
                PaymentLogMarkers.PG_DB_MISMATCH, pgOrderId, payment.getId(), order.getOrderStatus(), approval.paymentKey()
            );
            return new PgReconcileResult(savedPayment.getPaymentId(), PgReconcileOutcome.ORDER_NOT_CONFIRMABLE);
        }

        confirmOrderAndPublish(savedPayment, order, order.getMemberId());
        return new PgReconcileResult(savedPayment.getPaymentId(), PgReconcileOutcome.COMPLETED);
    }

    public void failPgConfirmation(String pgOrderId, PgConfirmResult result) {
        Payment payment = loadForUpdate(pgOrderId);

        recordTossDetail(payment.getPaymentId(), result.detail());

        if (result.failure() == PgConfirmFailure.UNKNOWN) {
            log.warn(
                "{} PG 승인 결과를 확인하지 못해 결제 상태를 바꾸지 않음 — pgOrderId={}, paymentId={}, paymentStatus={}, errorCode={}",
                PaymentLogMarkers.PG_CONFIRM_UNKNOWN, pgOrderId, payment.getId(), payment.getPaymentStatus(), result.errorCode()
            );
            return;
        }

        if (result.failure() != PgConfirmFailure.REJECTED) {
            return;
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.fail();
        paymentSavePort.save(payment);
    }

    public PaymentId completeOnSitePayment(ShopId shopId, PaymentId paymentId) {
        Payment payment = paymentLoadPort.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderTransitionService.load(payment.getOrderId());
        if (!shopId.equals(order.getShopId())) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new DomainException(DomainErrorCode.PAYMENT_NOT_PENDING);
        }

        if (!payment.getPaymentMethod().isOnSite()) {
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
            order.getMemberId(),
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

    private Payment loadForUpdate(String pgOrderId) {
        return paymentLoadPort.findByPgOrderIdForUpdate(pgOrderId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
    }

    private boolean isAlreadyAppliedApproval(Payment payment, PgConfirmResult result) {
        return payment.getPaymentStatus() == PaymentStatus.COMPLETED
            && payment.getPgTid() != null
            && payment.getPgTid().equals(result.paymentKey());
    }

    private Payment markApproved(Payment payment, PgProvider pgProvider, String pgOrderId, PgConfirmResult result) {
        payment.updatePgInfo(pgProvider, result.paymentKey(), pgOrderId);

        if (result.cardCompany() != null) {
            payment.updateCardInfo(result.cardCompany(), result.cardNumber(), result.installmentPlanMonths());
        }

        payment.complete(result.paymentKey(), result.approvedAt(), result.receiptUrl());
        return paymentSavePort.save(payment);
    }

    private void confirmOrderAndPublish(Payment savedPayment, Order order, MemberId memberId) {
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
    }

    private boolean isNotAwaitingPgApproval(PaymentStatus paymentStatus) {
        return paymentStatus != PaymentStatus.PENDING && paymentStatus != PaymentStatus.FAILED;
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
