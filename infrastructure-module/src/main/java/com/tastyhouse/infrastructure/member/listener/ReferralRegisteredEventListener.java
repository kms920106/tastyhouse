package com.tastyhouse.infrastructure.member.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.member.referral.domain.event.ReferralRegisteredEvent;

/**
 * 추천 등록 이벤트 리스너(크로스커팅).
 *
 * <p>추천 등록은 일반 가입과 소셜 가입(4종) 어느 경로에서도 발생하므로, 특정 api 모듈이 아니라
 * infrastructure-module에 두어 발행 경로와 무관하게 동일하게 반응하게 한다(분류 E).
 */
@Slf4j
@Component
public class ReferralRegisteredEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReferralRegisteredEvent event) {
        log.info("추천 등록 완료 — referralId={}, referrerId={}, refereeId={}, registeredAt={}",
            event.referralId().value(),
            event.referrerId().value(),
            event.refereeId().value(),
            event.registeredAt()
        );
    }
}
