package com.tastyhouse.infrastructure.coupon.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.coupon.event.MemberCouponIssuedEvent;
import com.tastyhouse.domain.coupon.event.MemberCouponUsedEvent;

/**
 * 쿠폰 발급·사용 이벤트 리스너.
 *
 * <p>쿠폰 발급은 admin(수동 발급)·이벤트 경유(가입·추천 보상 등), 사용은 web(주문 결제)에서 트리거되므로
 * 특정 api 모듈에 두면 다른 모듈 트리거 시 리스너가 누락된다. 따라서 크로스커팅 리스너로
 * infrastructure-module에 둔다(공통 지침 분류 E).
 */
@Component
public class CouponEventListener {

    private static final Logger log = LoggerFactory.getLogger(CouponEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberCouponIssuedEvent event) {
        log.info("쿠폰 발급 완료 — memberCouponId={}, memberId={}, couponId={}, issuedAt={}",
            event.memberCouponId().value(),
            event.memberId().value(),
            event.couponId().value(),
            event.issuedAt()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberCouponUsedEvent event) {
        log.info("쿠폰 사용 완료 — memberCouponId={}, memberId={}, couponId={}, usedAt={}",
            event.memberCouponId().value(),
            event.memberId().value(),
            event.couponId().value(),
            event.usedAt()
        );
    }
}
