package com.tastyhouse.infrastructure.payment.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.payment.event.PaymentCancelledEvent;
import com.tastyhouse.domain.payment.event.PaymentCompletedEvent;
import com.tastyhouse.domain.payment.event.RefundRequestedEvent;
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.point.service.PointLedgerService;

@Component
public class PaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final PointLedgerService pointLedgerService;
    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentEventListener(PointLedgerService pointLedgerService, PaymentConfirmationService paymentConfirmationService) {
        this.pointLedgerService = pointLedgerService;
        this.paymentConfirmationService = paymentConfirmationService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (!event.isOnSitePayment()) {
            return;
        }

        int earnRate = paymentConfirmationService.earnRate();
        int earnedPoint = paymentConfirmationService.calculateEarnedPoint(event.amount());
        pointLedgerService.earnPoints(
            event.memberId(),
            earnedPoint,
            "현장 현금 결제 적립 (" + earnRate + "%)"
        );

        log.info("현장 결제 포인트 적립 — memberId={}, earnedPoint={}", event.memberId().value(), earnedPoint);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCancelled(PaymentCancelledEvent event) {
        if (event.usedPoint() > 0) {
            pointLedgerService.refundPoints(event.memberId(), event.usedPoint());
            log.info("결제 취소 포인트 환급 — memberId={}, usedPoint={}", event.memberId().value(), event.usedPoint());
        }

        if (event.earnedPoint() > 0) {
            pointLedgerService.reclaimEarnedPoints(event.memberId(), event.earnedPoint());
            log.info("결제 취소 적립 포인트 회수 — memberId={}, earnedPoint={}",
                event.memberId().value(), event.earnedPoint());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRefundRequested(RefundRequestedEvent event) {
        log.info("환불 요청 접수 — refundId={}, paymentId={}, memberId={}, refundAmount={}, refundReason={}, requestedAt={}",
            event.refundId().value(),
            event.paymentId().value(),
            event.memberId().value(),
            event.refundAmount().value(),
            event.refundReason(),
            event.requestedAt()
        );
    }
}
