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
import com.tastyhouse.domain.payment.service.PaymentConfirmationService;
import com.tastyhouse.domain.point.service.PointLedgerService;

/**
 * 결제 완료·취소 이벤트를 받아 포인트를 연동하는 크로스커팅 리스너(분류 E).
 *
 * <p>infrastructure-module에 두는 이유: 결제는 web-api(회원 결제·취소)에서 트리거되지만, 이후 admin-api의
 * 환불·관리 경로가 같은 이벤트를 발행하게 되어도 포인트 연동이 누락되지 않아야 한다. 특정 api 모듈에
 * 리스너를 두면 다른 모듈이 이벤트를 발행할 때 연동이 빠지므로, 모든 실행 모듈이 공통으로 의존하는
 * infrastructure-module이 소유한다.
 *
 * <p>포인트 증감 규칙 자체는 도메인 서비스 {@link PointLedgerService}가 갖고, 적립액 계산은
 * {@link PaymentConfirmationService#calculateEarnedPoint}가 단일 원천이다(주문에 기록되는 적립 포인트와
 * 실제 적립액이 갈리지 않도록). 이 리스너는 이벤트 수신과 트랜잭션 경계(커밋 후 새 트랜잭션)만 담당한다.
 */
@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final PointLedgerService pointLedgerService;
    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentEventListener(PointLedgerService pointLedgerService, PaymentConfirmationService paymentConfirmationService) {
        this.pointLedgerService = pointLedgerService;
        this.paymentConfirmationService = paymentConfirmationService;
    }

    /**
     * 결제 완료 — 현장 결제만 적립 대상이다(PG 결제는 주문 접수 시점에 이미 처리됨).
     */
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

    /**
     * 결제 취소 — 사용한 포인트는 돌려주고, 적립된 포인트는 회수한다.
     */
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
}
