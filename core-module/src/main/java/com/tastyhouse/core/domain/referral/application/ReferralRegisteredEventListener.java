package com.tastyhouse.core.domain.referral.application;

import com.tastyhouse.core.domain.referral.domain.event.ReferralRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ReferralRegisteredEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReferralRegisteredEvent event) {
        log.info("추천 등록 완료 — referralId={}, referrerId={}, refereeId={}, registeredAt={}",
            event.referralId().value(),
            event.referrerId(),
            event.refereeId(),
            event.registeredAt()
        );
    }
}
