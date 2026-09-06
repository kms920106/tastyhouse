package com.tastyhouse.infrastructure.coupon.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.coupon.event.MemberCouponIssuedEvent;
import com.tastyhouse.domain.coupon.event.MemberCouponUsedEvent;

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
