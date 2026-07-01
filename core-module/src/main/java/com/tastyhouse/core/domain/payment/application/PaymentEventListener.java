package com.tastyhouse.core.domain.payment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.payment.domain.event.PaymentCancelledEvent;
import com.tastyhouse.core.domain.payment.domain.event.PaymentCompletedEvent;
import com.tastyhouse.core.domain.point.application.PointCommandService;
import com.tastyhouse.core.domain.point.application.dto.command.EarnPointCommand;
import com.tastyhouse.core.domain.point.application.dto.command.ReclaimPointCommand;
import com.tastyhouse.core.domain.point.application.dto.command.RefundPointCommand;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PointCommandService pointCommandService;

    private static final int CASH_POINT_EARN_RATE = 10;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void on(PaymentCompletedEvent event) {
        if (!event.isOnSitePayment()) {
            return;
        }

        int earnedPoint = (int) (event.amount().value() * CASH_POINT_EARN_RATE / 100.0);
        pointCommandService.getOrCreateMemberPoint(event.memberId());
        pointCommandService.earnPoints(new EarnPointCommand(
            event.memberId(),
            earnedPoint,
            "현장 현금 결제 적립 (" + CASH_POINT_EARN_RATE + "%)"
        ));

        log.info("Point earned. memberId: {}, earnedPoint: {}", event.memberId(), earnedPoint);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void on(PaymentCancelledEvent event) {
        if (event.usedPoint() > 0) {
            pointCommandService.refundPoints(new RefundPointCommand(event.memberId(), event.usedPoint()));
            log.info("Point refunded. memberId: {}, usedPoint: {}", event.memberId(), event.usedPoint());
        }

        if (event.earnedPoint() > 0) {
            pointCommandService.reclaimEarnedPoints(new ReclaimPointCommand(event.memberId(), event.earnedPoint()));
            log.info("Earned point reclaimed. memberId: {}, earnedPoint: {}", event.memberId(), event.earnedPoint());
        }
    }
}
