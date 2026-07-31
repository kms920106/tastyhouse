package com.tastyhouse.infrastructure.point.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.point.domain.event.PointEarnedEvent;
import com.tastyhouse.domain.point.domain.event.PointRefundedEvent;
import com.tastyhouse.domain.point.domain.event.PointUsedEvent;

/**
 * 포인트 변동 이벤트 리스너.
 *
 * <p>포인트 변동은 web(주문 결제)·admin(수동 조정)·이벤트 경유(결제 취소·추천 보상) 등 여러 모듈에서
 * 트리거되므로, 특정 api 모듈에 두면 다른 모듈 트리거 시 리스너가 누락된다. 따라서 크로스커팅 리스너로
 * infrastructure-module에 둔다(공통 지침 분류 E).
 */
@Slf4j
@Component
public class PointEventListener {

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
