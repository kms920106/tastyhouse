package com.tastyhouse.core.domain.point.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.point.domain.event.PointEarnedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointRefundedEvent;
import com.tastyhouse.core.domain.point.domain.event.PointUsedEvent;

@Slf4j
@Component
public class PointEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PointEarnedEvent event) {
        log.info("포인트 적립 완료 — memberId={}, pointAmount={}, reason={}, earnedAt={}",
            event.memberId().value(), event.pointAmount(), event.reason(), event.earnedAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PointUsedEvent event) {
        log.info("포인트 사용 완료 — memberId={}, pointAmount={}, usedAt={}",
            event.memberId().value(), event.pointAmount(), event.usedAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PointRefundedEvent event) {
        log.info("포인트 환불 완료 — memberId={}, pointAmount={}, refundedAt={}",
            event.memberId().value(), event.pointAmount(), event.refundedAt());
    }
}
