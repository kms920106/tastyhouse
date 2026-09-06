package com.tastyhouse.infrastructure.point.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.point.event.PointEarnedEvent;
import com.tastyhouse.domain.point.event.PointRefundedEvent;
import com.tastyhouse.domain.point.event.PointUsedEvent;

@Component
public class PointEventListener {
    private static final Logger log = LoggerFactory.getLogger(PointEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PointEarnedEvent event) {
        log.info("포인트 적립 완료 — memberId={}, pointAmount={}, reason={}, earnedAt={}",
            event.memberId().value(), event.pointAmount(), event.reason(), event.earnedAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PointUsedEvent event) {
        log.info("포인트 사용 완료 — memberId={}, pointAmount={}, usedAt={}",
            event.memberId().value(), event.pointAmount(), event.usedAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PointRefundedEvent event) {
        log.info("포인트 환불 완료 — memberId={}, pointAmount={}, refundedAt={}",
            event.memberId().value(), event.pointAmount(), event.refundedAt());
    }
}
