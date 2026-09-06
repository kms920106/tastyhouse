package com.tastyhouse.infrastructure.member.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.member.event.MemberRegisteredEvent;
import com.tastyhouse.domain.member.event.MemberWithdrawnEvent;

@Component
public class MemberEventListener {
    private static final Logger log = LoggerFactory.getLogger(MemberEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberRegisteredEvent event) {
        log.info("회원가입 완료 — memberId={}, username={}, registeredAt={}",
            event.memberId().value(), event.username(), event.registeredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MemberWithdrawnEvent event) {
        log.info("회원탈퇴 완료 — memberId={}, reason={}, withdrawnAt={}",
            event.memberId().value(), event.reason(), event.withdrawnAt());
    }
}
