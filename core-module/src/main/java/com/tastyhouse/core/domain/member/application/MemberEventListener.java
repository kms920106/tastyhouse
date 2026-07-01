package com.tastyhouse.core.domain.member.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.member.domain.event.MemberRegisteredEvent;
import com.tastyhouse.core.domain.member.domain.event.MemberWithdrawnEvent;

@Slf4j
@Component
public class MemberEventListener {

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
