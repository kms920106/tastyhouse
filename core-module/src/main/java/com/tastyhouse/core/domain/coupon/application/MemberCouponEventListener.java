package com.tastyhouse.core.domain.coupon.application;

import com.tastyhouse.core.domain.coupon.domain.event.MemberCouponIssuedEvent;
import com.tastyhouse.core.domain.coupon.domain.event.MemberCouponUsedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class MemberCouponEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberCouponIssuedEvent event) {
        log.info("쿠폰 발급 완료 — memberCouponId={}, memberId={}, couponId={}, issuedAt={}",
            event.memberCouponId().value(),
            event.memberId(),
            event.couponId().value(),
            event.issuedAt()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberCouponUsedEvent event) {
        log.info("쿠폰 사용 완료 — memberCouponId={}, memberId={}, couponId={}, usedAt={}",
            event.memberCouponId().value(),
            event.memberId(),
            event.couponId().value(),
            event.usedAt()
        );
    }
}
